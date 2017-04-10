package ft.sim.simulation;

import ft.sim.world.map.GlobalMap;
import java.util.Random;

/**
 * Created by Sina on 31/03/2017.
 */
public class Disruptor {

  Random randomGenerator;

  Disruptor(long seed){
    randomGenerator = new Random(seed);
  }

  public void disruptTheWorld(GlobalMap map){
    //TODO
  }

}
