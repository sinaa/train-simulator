package ft.sim.world.map;

import ft.sim.world.connectables.Track;

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

}
