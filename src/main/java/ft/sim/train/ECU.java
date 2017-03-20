package ft.sim.train;

import ft.sim.simulation.Tickable;
import ft.sim.world.journey.Journey;
import ft.sim.world.journey.JourneyPath;

/**
 * Created by Sina on 07/03/2017.
 */
public class ECU implements Tickable {

  Engine engine;

  public ECU(Journey journey, Engine engine){
    this.engine = engine;
    createWorldModel(journey);
  }

  private void createWorldModel(Journey journey){
    JourneyPath jp = journey.getJourneyPath();

  }

  public void tick(double time){

  }

}
