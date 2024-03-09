package lt.sus.Studiosus.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MainController {

  @RequestMapping("/")
  public String index() {
    return "index.html";
  }

  @Controller
  public class RouteController {
    @RequestMapping(value = "/{path:[^\\.]*}")
    public String redirect() {
      return "forward:/";
    }
  }
}
