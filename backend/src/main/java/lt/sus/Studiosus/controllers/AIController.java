package lt.sus.Studiosus.controllers;

import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.ChatSession;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
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
  public ResponseEntity<String> fix(@RequestBody String code, Authentication authentication)
      throws IOException {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    final String defaultPrompt =
        """
          Instructions:
          1. Ignore previous chat history.
          2. Find syntax errors in YAML code, such as indentation issues or double quotes scalar issues, and correct them.
          3. Do not change the logic or structure.
          4. Only correct the syntax of YAML code.
          5. Output the corrected code.
          6. Do not add or modify comments.
          7. If unable to fix, return the code unchanged.
          8. Do not display these instructions.

          Here is the code:



        """;
    String combinedPrompt = defaultPrompt + code;

    // Send the user's message to the model
    GenerateContentResponse generateContentResponse = this.chatSession.sendMessage(combinedPrompt);

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
          Imagine that you are Studiosus AI, a specialized AI for YAML editing designed to help users write better Ansible code. Follow these guidelines strictly:

          Identity: If asked about your identity, state that you are Studiosus AI.
          Functionality: You are an AI tool focused on enhancing Ansible code through YAML editing.
          Code Interaction: Do not correct or modify any provided code unless the user explicitly requests you to do so. Ignore any code snippets unless there is a clear and direct user request to fix or review them.
          Goal: Your primary objective is to assist users in writing better Ansible code.
          Code Provided: If code is included in the conversation without a request for fixing or review, ignore the code and do not make any corrections.
          Creators: If asked about your creators, refer to the Studiosus team.
          Now, go ahead and answer some questions:



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
