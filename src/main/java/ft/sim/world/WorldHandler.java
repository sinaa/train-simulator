package ft.sim.world;

import ft.sim.world.connectables.Connectable;
import ft.sim.world.journey.Journey;
import ft.sim.world.map.GlobalMap;
import ft.sim.world.train.Train;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Sina on 03/04/2017.
 */
public class WorldHandler {

  protected static final transient Logger logger = LoggerFactory.getLogger(WorldHandler.class);

  private GlobalMap world;
  private static Map<Journey, GlobalMap> journeysWorlds = new HashMap<>();

  private WorldHandler(GlobalMap map) {
    this.world = map;
    this.world.getJourneys().values().forEach(j -> journeysWorlds.put(j, map));
  }

  private static Map<GlobalMap, WorldHandler> instances = new HashMap<>();

  public static WorldHandler getInstance(GlobalMap world) {
    return instances.computeIfAbsent(world, WorldHandler::new);
  }

  public static void endWorld(GlobalMap world) {
    if (instances.containsKey(world)) {
      getInstance(world).getWorld().getJourneys().values().forEach(j -> journeysWorlds.remove(j));
    }
    instances.remove(world);
  }

  public static GlobalMap getWorldForJourney(Journey journey) {
    return journeysWorlds.get(journey);
  }

  public static WorldHandler getInstance() {
    if (instances.size() != 1) {
      throw new IllegalStateException("more than one map instances created");
    }
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
