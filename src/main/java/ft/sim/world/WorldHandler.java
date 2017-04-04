package ft.sim.world;

import ft.sim.simulation.Tickable;
import ft.sim.world.journey.Journey;
import ft.sim.world.map.GlobalMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sina on 03/04/2017.
 */
public class WorldHandler {

  private GlobalMap world;

  private WorldHandler(GlobalMap map) {
    this.world = map;
  }

  private static Map<GlobalMap, WorldHandler> instances = new HashMap<>();

  public static WorldHandler getInstance(GlobalMap world) {
    return instances.computeIfAbsent(world, WorldHandler::new);
  }

  public static void endWorld(GlobalMap world){
    instances.remove(world);
  }

  public static WorldHandler getInstance(){
    if(instances.size()!=1)
      throw new IllegalStateException("more than one map instances created");
    return instances.values().iterator().next();
  }

  public GlobalMap getWorld() {
    return world;
  }

  public void tick(double time) {
    // tick all journeys
    for (Map.Entry<Integer, Journey> entry : world.getJourneys().entrySet()) {
      Journey j = entry.getValue();
      j.tick(time);
      j.getJourneyInformation().update(j);
    }

    // tick all stations
    world.getStations().forEach((id, station) -> station.tick(time));
  }
}
