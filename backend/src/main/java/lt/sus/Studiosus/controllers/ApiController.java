package lt.sus.Studiosus.controllers;

import java.util.HashMap;
import java.util.Map;
import lt.sus.Studiosus.model.AuthenticatedUser;
import lt.sus.Studiosus.model.Project;
import lt.sus.Studiosus.model.User;
import lt.sus.Studiosus.repository.UserRepository;
import lt.sus.Studiosus.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(allowCredentials = "true", originPatterns = "*")
@RestController
@RequestMapping("/api")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiController {

  @Autowired private Environment env;
  @Autowired private ProjectService projectService;
  @Autowired private UserRepository userRepository;

  @GetMapping("/loginOptions")
  public Map<String, Boolean> loginOptions() {
    Map<String, Boolean> loginOptions = new HashMap<>();
    loginOptions.put(
        "github",
        env.containsProperty("github.client-id") && env.containsProperty("github.client-secret"));
    loginOptions.put(
        "gitlab",
        env.containsProperty("gitlab.client-id") && env.containsProperty("gitlab.client-secret"));
    return loginOptions;
  }

  @GetMapping("/profile")
  public ResponseEntity<Map<String, String>> profile(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    Map<String, String> profileInfo = new HashMap<>();

    if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
      profileInfo.put("name", oauth2User.getAttribute("name"));
      profileInfo.put("email", oauth2User.getAttribute("email"));
      profileInfo.put("imageLink", oauth2User.getAttribute("avatar_url"));
    } else if (authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser) {
      profileInfo.put("name", authenticatedUser.getEmail());
      profileInfo.put("email", authenticatedUser.getUsername());
      profileInfo.put("imageLink", authenticatedUser.getAvatar());
    }
    return ResponseEntity.status(HttpStatus.OK).body(profileInfo);
  }

  @GetMapping("/createProject/{name}")
  public ResponseEntity<Project> createProject(
      @PathVariable String name, Authentication authentication) {
    String email = "";
    if (authentication != null && authentication.isAuthenticated()) {
      if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
        email = oauth2User.getAttribute("email");
      } else if (authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser) {
        email = authenticatedUser.getEmail();
      }
    } else {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    Project savedProject = projectService.createProjectForUser(email, name);

    return ResponseEntity.status(HttpStatus.CREATED).body(savedProject);
  }

  @GetMapping("/deleteProject/{id}")
  public ResponseEntity<String> deleteProject(
      @PathVariable Long id, Authentication authentication) {

    String email = "";
    if (authentication != null && authentication.isAuthenticated()) {
      if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
        email = oauth2User.getAttribute("email");
      } else if (authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser) {
        email = authenticatedUser.getEmail();
      }
    } else {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
    }

    try {
      projectService.deleteProjectForUser(email, id);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body("User does not have permission to delete the project");
    }

    return ResponseEntity.status(HttpStatus.OK).body("Deleted project successfully");
  }

  @GetMapping("/projects")
  public ResponseEntity<?> getProjects(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String email = "";
    if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
      email = oauth2User.getAttribute("email");
    } else if (authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser) {
      email = authenticatedUser.getEmail();
    }

    User user =
        userRepository
            .findUserByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    return ResponseEntity.status(HttpStatus.OK).body(projectService.getProjectsForUser(user));
  }
}
