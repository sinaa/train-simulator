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

  public static boolean trackHasTrain(GlobalMap map, Track track) {
    return map.getJourneys().values().stream()
        .anyMatch(
            j -> j.getJourneyPosition().getConnectablesOccupied().stream().anyMatch(c -> c == track)
        );
  }

}
