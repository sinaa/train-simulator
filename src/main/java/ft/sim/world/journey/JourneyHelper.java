package ft.sim.world.journey;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import ft.sim.world.connectables.Connectable;
import ft.sim.world.map.GlobalMap;
import ft.sim.world.train.Train;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Created by sina on 09/05/2017.
 */
public class JourneyHelper {

  private static Map<GlobalMap, JourneyHelper> instances = new HashMap<>();
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

  public Train getTrainFollowing(Train train){
    return getTrainsFollowingEachOther().get(train);
  }

  public Train getTrainBehind(Train train){
    return getTrainsFollowingEachOther().inverse().get(train);
  }

  public BiMap<Train, Train> getTrainsFollowingEachOther() {
    if (trailingTrains.size() + 1 == world.getTrains().size()) {
      return trailingTrains;
    }
    trailingTrains.clear();

    Map<Journey, Journey> journeyTrail = getJourneysFollowingEachOther();
    journeyTrail.forEach((key,value) -> trailingTrains.put(key.getTrain(),value.getTrain()));

    return trailingTrains;
  }

  public Map<Journey, Journey> getJourneysFollowingEachOther() {
    if (trailingJourneys.size() + 1 == world.getJourneys().size()) {
      return trailingJourneys;
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
