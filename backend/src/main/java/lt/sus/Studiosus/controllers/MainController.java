package lt.sus.Studiosus.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lt.sus.Studiosus.model.AuthenticatedUser;
import lt.sus.Studiosus.model.User;
import lt.sus.Studiosus.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MainController {

  @Autowired private UserDetailsServiceImpl userDetailsService;

  private final PasswordEncoder encoder;

  public MainController(PasswordEncoder passwordEncoder) {
    this.encoder = passwordEncoder;
  }

  @RequestMapping("/")
  public String index() {
    return "index.html";
  }

  @PostMapping("/register")
  public String processRegister(
      @RequestParam String username, @RequestParam String email, @RequestParam String password) {
    try {
      // Process the registration using form data
      User user = new User();
      user.setUsername(username);
      user.setEmail(email);
      user.setPassword(encoder.encode(password));
      user.setRoles("ROLE_USER");
      userDetailsService.save(user);
      return "redirect:/login"; // Registration successful, redirect to login
    } catch (IllegalArgumentException e) {
      // Redirect to registration page with error message
      String exceptionType = e.getClass().getSimpleName();
      String errorMsg = "exception=" + URLEncoder.encode(exceptionType, StandardCharsets.UTF_8);
      return "redirect:/register?" + errorMsg;
    }
  }

  @GetMapping("/profile")
  public String profile(Model model, Authentication authentication) {
    if (authentication != null && authentication.isAuthenticated()) {
      if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
        model.addAttribute("name", oauth2User.getAttribute("name"));
        model.addAttribute("email", oauth2User.getAttribute("email"));
        model.addAttribute("imageLink", oauth2User.getAttribute("avatar_url"));
      } else if (authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser) {
        model.addAttribute("name", authenticatedUser.getEmail());
        model.addAttribute("email", authenticatedUser.getUsername());
        model.addAttribute("imageLink", authenticatedUser.getAvatar());
      }
      return "profile";
    } else {
      return "redirect:/login"; // Redirect to the login page if not authenticated
    }
  }

  @GetMapping("/logout")
  public String logout(HttpServletRequest request, HttpServletResponse response) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null) {
      new SecurityContextLogoutHandler().logout(request, response, auth);
    }
    return "redirect:/login";
  }

  @Controller
  public class RouteController {
    @RequestMapping(value = "/{path:[^\\.]*}")
    public String redirect() {
      return "forward:/";
    }
  }
}
