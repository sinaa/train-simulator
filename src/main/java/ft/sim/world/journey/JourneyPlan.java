package ft.sim.world.journey;

/**
 * Created by Sina on 15/03/2017.
 */
public class JourneyPlan {

  JourneyPath journeyPath;

  public JourneyPlan(Journey journey) {
    journeyPath = journey.getJourneyPath();
  }

  public JourneyPath getJourneyPath() {
    return journeyPath;
  }
}
