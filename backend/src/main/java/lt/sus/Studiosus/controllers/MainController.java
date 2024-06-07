package lt.sus.Studiosus.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import lt.sus.Studiosus.model.Log;
import lt.sus.Studiosus.model.User;
import lt.sus.Studiosus.model.enums.LogLevel;
import lt.sus.Studiosus.service.LoggingService;
import lt.sus.Studiosus.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MainController {

  @Value("${app.frontendUrl}")
  private String frontendUrl;

  private final PasswordEncoder encoder;
  private final UserDetailsServiceImpl userDetailsService;
  private final LoggingService loggingService;

  public MainController(
      PasswordEncoder passwordEncoder,
      UserDetailsServiceImpl userDetailsService,
      LoggingService loggingService) {
    this.encoder = passwordEncoder;
    this.userDetailsService = userDetailsService;
    this.loggingService = loggingService;
  }

  @GetMapping("/")
  public void index(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.sendRedirect(frontendUrl);
  }

  @PostMapping("/register")
  public void processRegister(
      @RequestParam String username,
      @RequestParam String email,
      @RequestParam String password,
      HttpServletRequest request,
      HttpServletResponse response)
      throws IOException {
    if (username.length() > 64) {
      String errorMsg = "UsernameTooLong";
      response.sendRedirect(frontendUrl + "/register?exception=" + errorMsg);
      return;
    }

    if (email.length() > 64) {
      String errorMsg = "EmailTooLong";
      response.sendRedirect(frontendUrl + "/register?exception=" + errorMsg);
      return;
    }

    if (password.length() > 64) {
      String errorMsg = "PasswordTooLong";
      response.sendRedirect(frontendUrl + "/register?exception=" + errorMsg);
      return;
    }

    try {
      // Process the registration using form data
      User user = new User();
      user.setUsername(username);
      user.setEmail(email);
      user.setPassword(encoder.encode(password));
      user.setRoles("ROLE_USER");
      userDetailsService.save(user);

      loggingService.saveLogToDatabase(
          new Log(
              "Successful User registration: %s".formatted(user.getUsername()),
              LogLevel.INFO,
              LocalDateTime.now()));

      // Registration successful, redirect to login
      response.sendRedirect(frontendUrl + "/login");
    } catch (IllegalArgumentException e) {
      // Redirect to registration page with error message
      String exceptionType = e.getClass().getSimpleName();
      String errorMsg = "exception=" + URLEncoder.encode(exceptionType, StandardCharsets.UTF_8);

      loggingService.saveLogToDatabase(
          new Log(
              "Unsuccessful User registration with error: %s".formatted(exceptionType),
              LogLevel.INFO,
              LocalDateTime.now()));

      response.sendRedirect(frontendUrl + "/register?" + errorMsg);
    }
  }

  @GetMapping("/logout")
  public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null) {
      new SecurityContextLogoutHandler().logout(request, response, auth);
    }
    Cookie cookie = new Cookie("JSESSIONID", null);
    cookie.setMaxAge(0);
    cookie.setPath(request.getContextPath());
    response.addCookie(cookie);
    response.sendRedirect(frontendUrl + "/login");
  }
}
