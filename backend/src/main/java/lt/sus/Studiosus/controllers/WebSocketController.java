package lt.sus.Studiosus.controllers;

import static lt.sus.Studiosus.service.UserDetailsServiceImpl.getEmail;

import java.util.Set;
import lt.sus.Studiosus.model.AuthenticatedUser;
import lt.sus.Studiosus.model.FileContent;
import lt.sus.Studiosus.model.enums.ResourceType;
import lt.sus.Studiosus.service.ActiveFileRegistry;
import lt.sus.Studiosus.service.ActiveUserRegistry;
import lt.sus.Studiosus.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

  private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);
  private final ActiveUserRegistry activeUserRegistry;
  private final ActiveFileRegistry activeFileRegistry;
  private final SimpMessageSendingOperations messagingTemplate;
  private final ProjectService projectService;

  public WebSocketController(
      ActiveUserRegistry activeUserRegistry,
      SimpMessageSendingOperations messagingTemplate,
      ActiveFileRegistry activeFileRegistry,
      ProjectService projectService) {
    this.activeUserRegistry = activeUserRegistry;
    this.messagingTemplate = messagingTemplate;
    this.activeFileRegistry = activeFileRegistry;
    this.projectService = projectService;
  }

  @MessageMapping("/file.getFileContent/{projectId}/file/{fileId}")
  @SendToUser("/topic/{projectId}/file/{fileId")
  public String getFileContent(
      @DestinationVariable String projectId,
      @DestinationVariable String fileId,
      Authentication authentication) {

    String email = getEmail(authentication);

    // Check if the user has the necessary permissions to view the file
    if (!projectService.canUserViewResource(
        email, Long.parseLong(projectId), Long.parseLong(fileId), ResourceType.FILE)) {
      messagingTemplate.convertAndSend(
          "/topic/" + projectId + "/errors/" + fileId,
          "You do not have permission to view this file");
      return null;
    }

    activeFileRegistry.removeUserFromOtherFiles(projectId, fileId, email);
    activeFileRegistry.addUserToFile(projectId, fileId, email);

    messagingTemplate.convertAndSend(
        "/topic/" + projectId + "/file/" + fileId,
        activeFileRegistry.getFileContent(projectId, fileId));

    // Send the updated list of active files to the topic
    messagingTemplate.convertAndSend(
        "/topic/" + projectId + "/activeFiles", activeFileRegistry.getProjectFiles(projectId));

    return activeFileRegistry.getFileContent(projectId, fileId);
  }

  @MessageMapping("/file.updateFile/{projectId}/file/{fileId}")
  @SendTo("/topic/{projectId}/file/{fileId}")
  public String updateFileContent(
      @DestinationVariable String projectId,
      @DestinationVariable String fileId,
      @Payload FileContent fileContent,
      Authentication authentication) {

    String email = getEmail(authentication);

    // Check if the user has the necessary permissions to view the file
    if (!projectService.canUserEditResource(
        email, Long.parseLong(projectId), Long.parseLong(fileId), ResourceType.FILE)) {
      messagingTemplate.convertAndSend(
          "/topic/" + projectId + "/errors/" + fileId,
          "You do not have permission to edit this file");
      return null;
    }

    activeFileRegistry.setFileContent(projectId, fileId, fileContent.getContent());
    messagingTemplate.convertAndSend(
        "/topic/" + projectId + "/file/" + fileId,
        activeFileRegistry.getFileContent(projectId, fileId));

    // Send the updated list of active files to the topic
    messagingTemplate.convertAndSend(
        "/topic/" + projectId + "/activeFiles", activeFileRegistry.getProjectFiles(projectId));

    return activeFileRegistry.getFileContent(projectId, fileId);
  }

  @MessageMapping("/chat.addUser/{projectId}")
  @SendTo("/topic/{projectId}/public")
  public void addUser(
      @DestinationVariable String projectId,
      SimpMessageHeaderAccessor headerAccessor,
      Authentication authentication) {
    String username = "";

    if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
      username = oauth2User.getAttribute("name");
    } else if (authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser) {
      username = authenticatedUser.getUsername();
    }

    headerAccessor.getSessionAttributes().put("username", username);
    headerAccessor.getSessionAttributes().put("projectId", projectId);
    activeUserRegistry.addUser(projectId, username);

    // Send the list of connected users to all connected users
    messagingTemplate.convertAndSend(
        "/topic/" + projectId + "/users", activeUserRegistry.getUsers(projectId));
  }

  @MessageMapping("/chat.structureChange/{projectId}")
  @SendTo("/topic/{projectId}/structureChange")
  public void notifyStructureChange(
      @DestinationVariable String projectId,
      SimpMessageHeaderAccessor headerAccessor,
      Authentication authentication) {
    String username = "";

    if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
      username = oauth2User.getAttribute("name");
    } else if (authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser) {
      username = authenticatedUser.getUsername();
    }

    // Notify all users that the project structure has changed
    messagingTemplate.convertAndSend("/topic/" + projectId + "/structureChange", "");
  }

  @MessageMapping("/file.getActiveFiles/{projectId}")
  @SendToUser("/topic/{projectId}/activeFiles")
  public Set<String> getFileContent(
      @DestinationVariable String projectId, Authentication authentication) {

    String email = getEmail(authentication);

    // Check if the user has the necessary permissions to view the file
    if (!projectService.canUserViewProject(
        email, Long.parseLong(projectId), Long.parseLong(projectId), ResourceType.PROJECT)) {
      //      messagingTemplate.convertAndSend(
      //          "/topic/" + projectId + "/errors/" + email, "You do not have permission this
      // project");
      return null;
    }

    messagingTemplate.convertAndSend(
        "/topic/" + projectId + "/activeFiles", activeFileRegistry.getProjectFiles(projectId));

    return activeFileRegistry.getProjectFiles(projectId);
  }
}
