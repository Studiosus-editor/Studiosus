package lt.sus.Studiosus.controllers;

import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.ChatSession;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lt.sus.Studiosus.dto.ChatRequest;
import lt.sus.Studiosus.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@CrossOrigin(
    allowCredentials = "true",
    originPatterns = "*",
    allowedHeaders = "*",
    methods = {
      RequestMethod.DELETE,
      RequestMethod.GET,
      RequestMethod.POST,
      RequestMethod.PUT,
      RequestMethod.OPTIONS
    })
@RequiredArgsConstructor
public class AIController {

  // Inject generative model instead of returning it from a bean,
  // so that a new chat session can be created manually without restarting backend
  private final GenerativeModel generativeModel;
  private ChatSession chatSession;
  private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

  // automatically create a new chat session when the controller is created
  // Its better, than relying on a bean
  @Autowired
  public void setChatSession() {
    this.chatSession = new ChatSession(generativeModel);
  }

  @PostMapping("/fix")
  public ResponseEntity<String> chat(ChatRequest chatRequest, Authentication authentication)
      throws IOException {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    final String defaultPrompt =
        """
        Ignore if there is any previous chat history
        Instructions:

        Please fix any errors in the provided Ansible playbook and return the corrected code. Do not add any additional comments or explanations.

        If the text following 'Code:' is not an Ansible playbook, return the same text. For instance, if the input is 'test', return 'test'. If there is no input, return an empty string.

        Return only text after 'Code:' do not return word 'Code:'.

        Do not include the 'Instructions' section in the response.

        If you determine that the code cannot be fixed, return the original code.

        Remember that if you can't see any '- name:' tag it is likely that the code is not an Ansible playbook.

        Please only address obvious issues and do not make assumptions about the code's intent or functionality.
        Also do not create any new variables or ansible stages.

        Code:

        """;
    final String data = chatRequest.getData();

    // Send the user's message to the model
    GenerateContentResponse generateContentResponse =
        this.chatSession.sendMessage(defaultPrompt + "\n" + data);

    // Retrieve the model's response
    String response = ResponseHandler.getText(generateContentResponse);

    logger.info("Chat response: {}", response);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @PostMapping("/chat")
  public synchronized ResponseEntity<String> chat(
      @RequestBody String prompt, Authentication authentication) throws IOException {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    String defaultMessage =
        """
        Imagine that you are Studiosus AI, not Bard, if someone asks you what AI you are, say that you are Studiosus AI.
        Your whole goal is to help people write better Ansible code.
        If user inputs CODE, STRICTLY IGNORE it, UNLESS IT IS A REQUEST to FIX the code.
        If someone ask what are your creators, say Studiosus team.
        Now go ahead and answer some questions:


        """;

    String combinedPrompt = defaultMessage + prompt;
    GenerateContentResponse generateContentResponse = this.chatSession.sendMessage(combinedPrompt);
    String response = ResponseHandler.getText(generateContentResponse);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @PutMapping("/newChat")
  public ResponseEntity<String> startNewChat(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    this.chatSession = new ChatSession(generativeModel);

    return ResponseEntity.status(HttpStatus.OK).body("New chat session started.");
  }
}
