package ft.sim.simulation;

import ft.sim.world.map.GlobalMap;
import java.util.Random;

/**
 * Created by Sina on 31/03/2017.
 */
public class Disruptor {

  private static Disruptor instance;
  private Random randomGenerator;

  private Disruptor(GlobalMap map) {
    randomGenerator = new Random((int) map.getConfiguration("seed"));
  }

  public static Disruptor getInstance(GlobalMap map) {
    if (instance == null) {
      instance = new Disruptor(map);
    }
    return instance;
  }

  public Disruptor(long seed) {
    randomGenerator = new Random(seed);
  }

  /**
   * Randomly check whether an item should be disrupted.
   *
   * @param ratio (e.g., a ratio of 3 will 3% of the times be true)
   */
  public boolean shouldDisrupt(int ratio) {
    int chance = randomGenerator.nextInt(100);
    return chance < ratio;
  }

}
