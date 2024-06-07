package lt.sus.Studiosus.controllers;

import lt.sus.Studiosus.model.AuthenticatedUser;
import lt.sus.Studiosus.model.ChatMessage;
import lt.sus.Studiosus.service.ActiveFileRegistry;
import lt.sus.Studiosus.service.ActiveUserRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

  private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

  private final SimpMessageSendingOperations messagingTemplate;
  private final ActiveUserRegistry activeUserRegistry;
  private final ActiveFileRegistry activeFileRegistry;

  public WebSocketEventListener(
      SimpMessageSendingOperations messagingTemplate,
      ActiveUserRegistry activeUserRegistry,
      ActiveFileRegistry activeFileRegistry) {
    this.messagingTemplate = messagingTemplate;
    this.activeUserRegistry = activeUserRegistry;
    this.activeFileRegistry = activeFileRegistry;
  }

  @EventListener
  public void handleWebSocketConnectListener(SessionConnectedEvent event) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
    String projectId = (String) headerAccessor.getSessionAttributes().get("projectId");
    if (projectId != null) {
      logger.info("User Connected to Project : " + projectId);
    }
  }

  @EventListener
  public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

    String username = (String) headerAccessor.getSessionAttributes().get("username");
    String projectId = (String) headerAccessor.getSessionAttributes().get("projectId");
    String email = "";
    Authentication auth =
        (Authentication) headerAccessor.getSessionAttributes().get("AUTHENTICATION");
    if (auth.getPrincipal() instanceof OAuth2User oauth2User) {
      email = oauth2User.getAttribute("email");
    } else if (auth.getPrincipal() instanceof AuthenticatedUser authenticatedUser) {
      email = authenticatedUser.getEmail();
    }

    if (username != null && projectId != null) {
      logger.info("User Disconnected : " + username);

      ChatMessage chatMessage = new ChatMessage();
      chatMessage.setType(ChatMessage.MessageType.LEAVE);
      chatMessage.setSender(username);

      messagingTemplate.convertAndSend("/topic/" + projectId + "/public", chatMessage);

      activeFileRegistry.removeUserFromProject(projectId, email);
      messagingTemplate.convertAndSend(
          "/topic/" + projectId + "/activeFiles", activeFileRegistry.getProjectFiles(projectId));

      // Send the updated list of connected users to all connected users
      activeUserRegistry.removeUser(projectId, username);
      messagingTemplate.convertAndSend(
          "/topic/" + projectId + "/users", activeUserRegistry.getUsers(projectId));
    }
  }
}
