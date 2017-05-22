package ft.sim.world.map;

import ft.sim.world.connectables.Track;
import ft.sim.world.placeables.ActiveBalise;
import ft.sim.world.placeables.Placeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

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

  public static List<String> getMaps() {
    List<String> maps = new ArrayList<>();
    ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    try {
      Resource[] resources = resolver.getResources("maps/*.yaml");
      Arrays.stream(resources).map(Resource::getFilename).map(f -> f.replace(".yaml", ""))
          .filter(s -> !s.equals("defaults"))
          .forEach(maps::add);
    } catch (IOException e) {
      e.printStackTrace();
    }
    File[] localYamls = new File(".").listFiles(
        (dir, name) -> name.toLowerCase().endsWith(".yaml")
    );
    Arrays.stream(localYamls).forEach(f -> maps.add(f.getName().replace(".yaml", "")));

    return maps;
  }

  static double copyActiveBalises(Track copyFrom, Track copyTo) {
    for (Entry<Integer, Placeable> e : copyFrom.getPlaceables().entrySet()) {
      int position = e.getKey();
      Placeable placeable = e.getValue();
      if (!(placeable instanceof ActiveBalise)) {
        continue;
      }
      int newPosition = (int) copyTo.getLength() - 1 - position;
      Placeable balise = new ActiveBalise();
      copyTo.placePlaceableOnSectionIndex(balise, newPosition);
    }
    return copyTo.getLength();
  }
}
