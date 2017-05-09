package ft.sim.world.placeables;

import ft.sim.simulation.Disruptable;
import ft.sim.world.journey.JourneyPath;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sina on 21/02/2017.
 */
public abstract class Balise implements Placeable, Disruptable {

  private Map<JourneyPath, Double> position = new HashMap<>();

  private double globalPosition = -1;

  public void setPosition(JourneyPath journeyPath, double miles) {
    position.put(journeyPath, miles);
  }

  public double getPosition(JourneyPath journeyPath) {
    return position.get(journeyPath);
  }

  public void setPosition(double miles) {
    globalPosition = miles;
  }

  public double getPosition() {
    return globalPosition;
  }
}
