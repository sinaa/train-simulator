package ft.sim.world.map;

import ft.sim.world.connectables.Connectable;
import ft.sim.world.connectables.Track;
import ft.sim.world.journey.Journey;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Created by sina on 11/04/2017.
 */
public class MapBuilderHelper {

  public static boolean hasTrain(GlobalMap map, Track track) {
    return map.getJourneys().values().stream()
        .anyMatch(
            j -> j.getJourneyPosition().getConnectablesOccupied().stream().anyMatch(c -> c == track)
        );
  }

  public static Map<Journey, Journey> getJourneysFollowingEachOther(GlobalMap map) {
    Map<Journey, Journey> journeyMap = new HashMap<>();

    Map<Connectable, TreeMap<Double, Journey>> graphRootJourneys = new HashMap<>();

    for (Journey journey : map.getJourneys().values()) {
      double distance = journey.getJourneyPosition().getHeadPosition()
          + journey.getJourneyPath().getDistanceFromGraphRoot();
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
        journeyMap.put(previousJourney, treeEntry.getValue());
        previousJourney = treeEntry.getValue();
      }
    }

    return journeyMap;
  }

  public static double getJourneyDistance(Journey j1, Journey j2) {
    double j1Distance =
        j1.getJourneyPosition().getHeadPosition() + j1.getJourneyPath().getDistanceFromGraphRoot();
    double j2Distance =
        j2.getJourneyPosition().getHeadPosition() + j2.getJourneyPath().getDistanceFromGraphRoot();
    return Math.abs(j2Distance - j1Distance);
  }

}
