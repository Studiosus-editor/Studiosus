package lt.sus.Studiosus.controllers;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {

  @Autowired private Environment env;

  @GetMapping("/profile")
  public Map<String, Object> profile(@AuthenticationPrincipal OAuth2User principal) {
    Map<String, Object> profile = new HashMap<>();
    profile.put("OAuthUserFullName", principal.getAttribute("name"));
    profile.put("OAuthUserEmail", principal.getAttribute("email"));
    return profile;
  }

  @GetMapping("/loginOptions")
  public Map<String, Boolean> loginOptions(@AuthenticationPrincipal OAuth2User principal) {
    Map<String, Boolean> loginOptions = new HashMap<>();
    loginOptions.put(
        "github",
        env.containsProperty("github.client-id") && env.containsProperty("github.client-secret"));
    loginOptions.put(
        "gitlab",
        env.containsProperty("gitlab.client-id") && env.containsProperty("gitlab.client-secret"));
    return loginOptions;
  }
}
