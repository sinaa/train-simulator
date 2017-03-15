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

  @RequestMapping("/")
  public String index(Model model) {
    return "home";
  }

  @RequestMapping("/hi")
  public String hi(
      @RequestParam(value = "name", required = false, defaultValue = "World") String name,
      Model model) {
    model.addAttribute("name", name);
    return "hi";
  }

  @RequestMapping("/simulation")
  public String simulation(Model model) {
    return "simulation";
  }

  @RequestMapping("/trains/start")
  public String trainsStart(Model model) {

      BasicSimulation.getInstance().startTrains();
      model.addAttribute("name", "Trains started");

    return "hi";
  }

}