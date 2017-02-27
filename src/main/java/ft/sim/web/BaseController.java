package ft.sim.web;

/**
 * Created by Sina on 20/02/2017.
 */

import ft.sim.simulation.BasicSimulation;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BaseController {

  public static BasicSimulation simulation;

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

  @RequestMapping("/simulate")
  public String simulate(Model model) {
    if (simulation != null) {
      simulation.kill();
    }
    simulation = new BasicSimulation();
    return "index";
  }

  @RequestMapping("/trains/start")
  public String trainsStart(Model model) {
    if (simulation != null) {
      simulation.startTrains();
      model.addAttribute("name", "Trains started");
    }
    return "hi";
  }

}