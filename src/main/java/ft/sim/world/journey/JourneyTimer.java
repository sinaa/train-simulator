package ft.sim.world.journey;

import ft.sim.simulation.Tickable;

/**
 * Created by sina on 19/04/2017.
 */
public class JourneyTimer implements Tickable {

  private double time = 0;

  @Override
  public void tick(double time) {
    this.time += time;
  }

  public double getTime() {
    return time;
  }
}
