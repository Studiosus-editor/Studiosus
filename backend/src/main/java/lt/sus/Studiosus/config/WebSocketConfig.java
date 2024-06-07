package lt.sus.Studiosus.config;

import jakarta.servlet.http.HttpSession;
import java.util.Map;
import lt.sus.Studiosus.model.AuthenticatedUser;
import lt.sus.Studiosus.service.ProjectService;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private final ProjectService projectService;

  public WebSocketConfig(ProjectService projectService) {
    this.projectService = projectService;
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry
        .addEndpoint("/ws")
        .setAllowedOriginPatterns("*")
        .addInterceptors(new HttpSessionHandshakeInterceptor())
        .withSockJS();
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.setApplicationDestinationPrefixes("/app");
    registry.enableSimpleBroker("/topic"); // Enables a simple in-memory broker
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(
        new ChannelInterceptor() {
          @Override
          public Message<?> preSend(Message<?> message, MessageChannel channel) {
            StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
              Authentication auth =
                  (Authentication) accessor.getSessionAttributes().get("AUTHENTICATION");
              if (auth == null || !auth.isAuthenticated()) {
                throw new AccessDeniedException("Access Denied!");
              }
            } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
              String destination = accessor.getDestination();
              Long projectId =
                  Long.parseLong(
                      destination
                          .split("/")[
                          2]); // Assuming the destination is in the format "/topic/{projectId}/..."

              String email = "";

              Authentication auth =
                  (Authentication) accessor.getSessionAttributes().get("AUTHENTICATION");
              if (auth.getPrincipal() instanceof OAuth2User oauth2User) {
                email = oauth2User.getAttribute("email");
              } else if (auth.getPrincipal() instanceof AuthenticatedUser authenticatedUser) {
                email = authenticatedUser.getEmail();
              }

              try {
                if (!projectService.hasUserPermissionsToEditProject(projectId, email)) {
                  throw new AccessDeniedException("Access Denied!");
                }
              } catch (Exception e) {
                throw new AccessDeniedException("Access Denied!");
              }
            }
            return message;
          }
        });
  }

  private class HttpSessionHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(
        ServerHttpRequest request,
        ServerHttpResponse response,
        WebSocketHandler wsHandler,
        Map<String, Object> attributes)
        throws Exception {
      if (request instanceof ServletServerHttpRequest) {
        ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
        HttpSession session = servletRequest.getServletRequest().getSession();
        SecurityContext securityContext =
            (SecurityContext)
                session.getAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        if (securityContext != null) {
          Authentication auth = securityContext.getAuthentication();
          attributes.put("AUTHENTICATION", auth);
        }
      }
      return true;
    }

    @Override
    public void afterHandshake(
        ServerHttpRequest request,
        ServerHttpResponse response,
        WebSocketHandler wsHandler,
        Exception exception) {}
  }
}
