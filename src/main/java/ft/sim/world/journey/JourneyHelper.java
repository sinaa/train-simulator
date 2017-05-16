package ft.sim.world.journey;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import ft.sim.world.connectables.Connectable;
import ft.sim.world.connectables.Station;
import ft.sim.world.map.GlobalMap;
import ft.sim.world.train.Train;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sina on 09/05/2017.
 */
public class JourneyHelper {

  private static Map<GlobalMap, JourneyHelper> instances = new HashMap<>();
  protected transient final Logger logger = LoggerFactory.getLogger(JourneyHelper.class);
  private BiMap<Journey, Journey> trailingJourneys = HashBiMap.create();
  private BiMap<Train, Train> trailingTrains = HashBiMap.create();
  private GlobalMap world;

  private JourneyHelper(GlobalMap map) {
    this.world = map;
  }

  public static JourneyHelper getInstance(GlobalMap world) {
    return instances.computeIfAbsent(world, JourneyHelper::new);
  }

  public static double getJourneyDistanceBetween(Journey j1, Journey j2) {
    double j1Distance = j1.getHeadPositionFromRoot();
    double j2Distance = j2.getHeadPositionFromRoot();
    return Math.abs(j2Distance - j1Distance);
  }

  public Train getTrainFollowing(Train train) {
    return getTrainsFollowingEachOther().get(train);
  }

  public Optional<Train> getTrainBehind(Train train) {
    return Optional.ofNullable(getTrainsFollowingEachOther().inverse().get(train));
  }

  public BiMap<Train, Train> getTrainsFollowingEachOther() {
    if (trailingTrains.size() + 1 == world.getTrains().size()) {
      return trailingTrains;
    }
    trailingTrains.clear();

    Map<Journey, Journey> journeyTrail = getJourneysFollowingEachOther();
    journeyTrail.forEach((key, value) -> trailingTrains.put(key.getTrain(), value.getTrain()));

    return trailingTrains;
  }

  public Map<Journey, Journey> getJourneysFollowingEachOther() {
    if (trailingJourneys.size() + 1 == world.getJourneys().size()) {
      // for all journeys, check if none of the trains are at stations
      if (world.getJourneys().values().stream().allMatch(
          j -> j.getJourneyPosition().getConnectablesOccupied().stream()
              .noneMatch(c -> c instanceof Station))){
        return trailingJourneys;
      }
    }
    trailingJourneys.clear();

    Map<Connectable, TreeMap<Double, Journey>> graphRootJourneys = new HashMap<>();

    for (Journey journey : world.getJourneys().values()) {
      double distance = journey.getHeadPositionFromRoot();
      Connectable rootKey = journey.getJourneyPath().getGraphRootConnectable();
      TreeMap<Double, Journey> distanceTree = graphRootJourneys
          .computeIfAbsent(rootKey, k -> new TreeMap<>());
      distanceTree.put(distance, journey);
      graphRootJourneys.put(rootKey, distanceTree);
    }

    for (Entry<Connectable, TreeMap<Double, Journey>> entry : graphRootJourneys.entrySet()) {
      Connectable graphRootNode = entry.getKey();
      TreeMap<Double, Journey> distanceTree = entry.getValue();
      Journey previousJourney = null;
      for (Entry<Double, Journey> treeEntry : distanceTree.entrySet()) {
        if (previousJourney == null) {
          previousJourney = treeEntry.getValue();
          continue;
        }
        trailingJourneys.put(previousJourney, treeEntry.getValue());
        previousJourney = treeEntry.getValue();
      }
    }

    return trailingJourneys;
  }
}
