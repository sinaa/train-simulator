package ft.sim.train;

import ft.sim.world.Journey;
import ft.sim.world.JourneyPath;

/**
 * Created by Sina on 07/03/2017.
 */
public class ECU {

  Engine engine;

  public ECU(Journey journey, Engine engine){
    this.engine = engine;
    createWorldModel(journey);
  }

  private void createWorldModel(Journey journey){
    JourneyPath jp = journey.getJourneyPath();

  }

}
