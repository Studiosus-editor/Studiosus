package lt.sus.Studiosus.controllers;

import static lt.sus.Studiosus.service.MailSenderService.resetPasswordHtml;
import static lt.sus.Studiosus.service.UserDetailsServiceImpl.getEmail;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lt.sus.Studiosus.model.AuthenticatedUser;
import lt.sus.Studiosus.model.User;
import lt.sus.Studiosus.repository.UserRepository;
import lt.sus.Studiosus.service.MailSenderService;
import lt.sus.Studiosus.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
@RestController
@RequestMapping("/api")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiController {

  @Value("${app.frontendUrl}")
  private String frontendUrl;

  private final Environment env;
  private final UserRepository userRepository;
  private final PasswordEncoder encoder;
  private final MailSenderService mailService;
  private final UserDetailsServiceImpl userService;

  public ApiController(
      Environment env,
      UserRepository userRepository,
      PasswordEncoder encoder,
      MailSenderService mailService,
      UserDetailsServiceImpl userService) {
    this.env = env;
    this.userRepository = userRepository;
    this.encoder = encoder;
    this.mailService = mailService;
    this.userService = userService;
  }

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

  @PutMapping("/forgot-password")
  public ResponseEntity<String> forgotPassword(@RequestBody String email) {
    Optional<User> userOptional = userRepository.findUserByEmail(email);
    if (userOptional.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }

    User user = userOptional.get();
    user.setResetToken(UUID.randomUUID().toString());

    userRepository.save(user);

    mailService.sendNewMail(
        email,
        "Request to reset password",
        resetPasswordHtml(
            user.getUsername(), frontendUrl + "/reset-password/" + user.getResetToken()));

    return ResponseEntity.status(HttpStatus.OK).body("Email sent successfully");
  }

  @PutMapping("/reset-password/{resetToken}")
  public ResponseEntity<String> resetPassword(
      @PathVariable String resetToken, @RequestBody String password) {
    Optional<User> userOptional = userRepository.findUserByResetToken(resetToken);

    if (userOptional.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }

    User user = userOptional.get();
    user.setPassword(encoder.encode(password));
    user.setResetToken(null);
    userRepository.save(user);

    return ResponseEntity.status(HttpStatus.OK).body("Password reset successfully");
  }

  @PutMapping("/password:update")
  public ResponseEntity<String> changeUsersPassword(
      @RequestBody String newPassword, Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String email = getEmail(authentication);
    User user = userService.getUser(email);
    user.setPassword(encoder.encode(newPassword));
    userRepository.save(user);

    return ResponseEntity.status(HttpStatus.OK).body("Password updated successfully");
  }

  @PutMapping("/username:update")
  public ResponseEntity<String> changeUsername(
      @RequestBody String newUsername, Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String email = getEmail(authentication);
    User user = userService.getUser(email);
    user.setUsername(newUsername);
    userRepository.save(user);

    return ResponseEntity.status(HttpStatus.OK).body("Username updated successfully");
  }

  @DeleteMapping("/user")
  public ResponseEntity<String> deleteUserAccount(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String email = getEmail(authentication);
    User user = userService.getUser(email);
    userRepository.delete(user);

    return ResponseEntity.status(HttpStatus.OK).body("User deleted successfully");
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

      User user = userRepository.findUserByEmail(oauth2User.getAttribute("email")).orElseThrow();
      profileInfo.put("isAdmin", user.getRoles().equals("ROLE_ADMIN") ? "true" : "false");

    } else if (authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser) {
      profileInfo.put("name", authenticatedUser.getUsername());
      profileInfo.put("email", authenticatedUser.getEmail());
      profileInfo.put("imageLink", authenticatedUser.getAvatar());

      User user = userRepository.findUserByEmail(authenticatedUser.getEmail()).orElseThrow();
      profileInfo.put("isAdmin", user.getRoles().equals("ROLE_ADMIN") ? "true" : "false");
    }
    return ResponseEntity.status(HttpStatus.OK).body(profileInfo);
  }
}
