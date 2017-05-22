package ft.sim.statistics;

import ft.sim.world.map.GlobalMap;

/**
 * Created by sina on 19/05/2017.
 */
public class StatisticsController {

  private static StatisticsController instance;

  public static StatisticsController getInstance(GlobalMap map) {
    if(instance==null){
      instance = new StatisticsController(map);
    }
    return instance;
  }

  public static StatisticsController getInstance() {
    return instance;
  }

  private StatisticsController(GlobalMap map){

  }

}
