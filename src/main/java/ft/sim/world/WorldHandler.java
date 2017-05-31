package ft.sim.world;

import static ft.sim.statistics.StatisticsVariable.ACTIVE_TRAINS;
import static ft.sim.statistics.StatisticsVariable.MAX_ACTIVE_TRAINS;
import static ft.sim.statistics.StatisticsVariable.MIN_ACTIVE_TRAINS;
import static ft.sim.statistics.StatisticsVariable.MIN_STATION_TRAINS;
import static ft.sim.statistics.StatisticsVariable.STATION_TRAINS;
import static ft.sim.statistics.StatisticsVariable.TRAIN_SPEED;

import ft.sim.statistics.StatisticsItem;
import ft.sim.statistics.StatsHelper;
import ft.sim.world.journey.Journey;
import ft.sim.world.map.GlobalMap;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Sina on 03/04/2017.
 */
public class WorldHandler {

  protected static final transient Logger logger = LoggerFactory.getLogger(WorldHandler.class);
  private static final int SPEED_RECORD_TICK_INTERVAL = 10;
  private static Map<Journey, GlobalMap> journeysWorlds = new HashMap<>();
  private static Map<GlobalMap, WorldHandler> instances = new HashMap<>();
  private GlobalMap world;
  private double time = 0;
  private long tick = 0;

  private WorldHandler(GlobalMap map) {
    this.world = map;
    this.world.getJourneys().values().forEach(j -> journeysWorlds.put(j, map));
  }

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
      throw new IllegalStateException(
          instances.size() + " maps found! This method should be called with exactly 1 instance.");
    }
    return instances.values().iterator().next();
  }

  public GlobalMap getWorld() {
    return world;
  }

  public void tick(double time) {
    tick++;
    // tick all journeys
    for (Map.Entry<Integer, Journey> entry : world.getJourneys().entrySet()) {
      Journey j = entry.getValue();
      j.tick(time);
      j.getJourneyInformation().update(j);
    }

    // tick all stations
    world.getStations().forEach((id, station) -> station.tick(time));

    this.time += time;

    logWorldStatistics();
  }

  private void logWorldStatistics() {
    int numTrainsActive = (int) world.getJourneys().values().stream()
        .filter(Journey::isInProgress).filter(j -> !j.getTrain().isAtStation()).count();
    int numTrainsAtStation = (int) world.getJourneys().values().stream()
        .filter(Journey::isInProgress).filter(j -> j.getTrain().isAtStation()).count();

    // Track max active trains (updates existing data if higher)
    StatisticsItem stat = StatsHelper.getStatItem(MAX_ACTIVE_TRAINS);
    if (stat == null || numTrainsActive > (int) stat.getValue()) {
      StatsHelper.track(MAX_ACTIVE_TRAINS, numTrainsActive);
    }

    // Track min active trains (updates existing data if lower)
    StatisticsItem statMin = StatsHelper.getStatItem(MIN_ACTIVE_TRAINS);
    if (statMin == null || numTrainsActive < (int) statMin.getValue()) {
      StatsHelper.track(MIN_ACTIVE_TRAINS, numTrainsActive);
    }

    // Track min trains at station (updates existing data if lower)
    StatisticsItem statMinStation = StatsHelper.getStatItem(MIN_STATION_TRAINS);
    if (statMinStation == null || numTrainsAtStation < (int) statMinStation.getValue()) {
      StatsHelper.track(MIN_STATION_TRAINS, numTrainsAtStation);
    }

    if (tick % SPEED_RECORD_TICK_INTERVAL == 0) {
      StatsHelper.log(STATION_TRAINS, numTrainsAtStation);
      StatsHelper.log(ACTIVE_TRAINS, numTrainsActive);

      world.getJourneys().values().stream().filter(Journey::isInProgress).forEach(j -> {
        StatsHelper.logFor(TRAIN_SPEED, j.getTrain(), j.getTrain().getEngine().getSpeed());
      });
    }
  }

  public double getTime() {
    return time;
  }

  public long getTick() {
    return tick;
  }
}
