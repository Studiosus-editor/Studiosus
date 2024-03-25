package lt.sus.Studiosus.controllers;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiController {

  @Autowired private Environment env;

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
}
