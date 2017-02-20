package ft.sim.web;

/**
 * Created by Sina on 20/02/2017.
 */

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BaseController {

  @RequestMapping("/")
  public String index(Model model) {
    return "index";
  }

  @RequestMapping("/hi")
  public String hi(
      @RequestParam(value = "name", required = false, defaultValue = "World") String name,
      Model model) {
    model.addAttribute("name", name);
    return "hi";
  }

}