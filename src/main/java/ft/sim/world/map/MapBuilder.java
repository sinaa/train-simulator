package ft.sim.world.map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import ft.sim.train.Train;
import ft.sim.world.connectables.Connectable;
import ft.sim.world.connectables.Station;
import ft.sim.world.connectables.Track;
import ft.sim.world.placeables.FixedBalise;
import ft.sim.world.placeables.Placeable;

/**
 * Created by Sina on 30/03/2017.
 */
public class MapBuilder {

  private GlobalMap map = null;

  protected transient static final Logger logger = LoggerFactory.getLogger(MapBuilder.class);

  /*public static GlobalMap buildNewMap() {
    return buildNewMap(DEFAULT_MAP);
  }*/

  public static GlobalMap buildNewMap(String mapName) {
    return buildNewMap(mapName, new GlobalMap());
  }

  public static GlobalMap buildNewMap(String mapName, GlobalMap globalMap) {
    MapBuilder mb = new MapBuilder();
    mb.map = globalMap;
    try {
      if (!mapName.startsWith("maps/")) {
        mapName = "maps/" + mapName;
      }
      if (!mapName.endsWith(".yaml")) {
        mapName += ".yaml";
      }
      mb.importBasicMap(mapName);
      logger.warn("imported map");
    } catch (IOException e) {
      logger.error("failed to import map");
      e.printStackTrace();
      throw new IllegalStateException("Failed to import map!");
    }
    return mb.map;
  }

  public static List<String> getMaps() {
    List<String> maps = new ArrayList<>();
    ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    try {
      Resource[] resources = resolver.getResources("maps/*.yaml");
      Arrays.stream(resources).map(Resource::getFilename).map(f-> f.replace(".yaml","")).forEach(maps::add);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return maps;
  }

  private void importBasicMap(String mapYaml) throws IOException {
    Resource resource = new ClassPathResource(mapYaml);
    Yaml yaml = new Yaml();
    Map<String, Object> map = (Map<String, Object>) yaml.load(resource.getInputStream());

    createTracks((Map<String, Object>) map.get("tracks"));

    createStations((Map<String, Object>) map.get("stations"));

    createPlaceables((Map<String, Object>) map.get("placeables"));

    createSwitches((Map<String, Object>) map.get("switches"));

    createJourneyPaths((Map<String, Object>) map.get("journeyPaths"));

    createTrains((Map<String, Object>) map.get("trains"));

    createJourneys((Map<String, Object>) map.get("journeys"));
  }

  private void createStations(Map<String, Object> stations) {
    if (stations == null) {
      return;
    }
    for (Map.Entry<String, Object> station : stations.entrySet()) {
      int stationID = Integer.parseInt(station.getKey());
      Map<String, Integer> stationData = (Map<String, Integer>) station.getValue();
      Station s = new Station(stationData.get("capacity"), stationData.get("wait"));

      map.addStation(stationID, s);
    }
  }


  private void createJourneys(Map<String, Object> journeys) {
    if (journeys == null) {
      return;
    }
    for (Map.Entry<String, Object> j : journeys.entrySet()) {
      int journeyID = Integer.parseInt(j.getKey());
      Map<String, Object> journeyData = (Map<String, Object>) j.getValue();
      int trainID = (int) journeyData.get("train");
      int jpID = (int) journeyData.get("path");
      boolean isForward = (boolean) journeyData.get("isForward");

      map.addJourney(journeyID, jpID, trainID, isForward);
    }
  }

  private void createJourneyPaths(Map<String, Object> journeyPaths) {
    if (journeyPaths == null) {
      return;
    }
    for (Map.Entry<String, Object> journeyPath : journeyPaths.entrySet()) {
      int journeyPathID = Integer.parseInt(journeyPath.getKey());
      Map<String, Object> jData = (Map<String, Object>) journeyPath.getValue();
      List<Map<String, Object>> path = (List<Map<String, Object>>) jData.get("path");
      List<Connectable> connectables = new ArrayList<>();
      for (Map<String, Object> connectable : path) {
        int connectableID = (int) connectable.get("id");
        String connectableType = (String) connectable.get("type");
        switch (connectableType) {
          case "track":
            connectables.add(map.getTrack(connectableID));
            break;
          case "switch":
            connectables.add(map.getSwitch(connectableID));
            break;
          case "station":
            connectables.add(map.getStation(connectableID));
            break;
          default:
            throw new IllegalArgumentException(
                "Invalid journeyPath path-element type: " + connectableType);
        }
      }
      map.addJourneyPath(journeyPathID, connectables);
    }
  }

  private void createSwitches(Map<String, Object> switches) {
    if (switches == null) {
      return;
    }
    for (Map.Entry<String, Object> s : switches.entrySet()) {
      int switchID = Integer.parseInt(s.getKey());
      Map<String, Object> sData = (Map<String, Object>) s.getValue();
      List<Integer> left = (List<Integer>) sData.get("left");
      List<Integer> right = (List<Integer>) sData.get("right");

      int statusLeft = (int) sData.get("statusLeft");
      int statusRight = (int) sData.get("statusRight");

      map.addSwitch(switchID, left, right, statusLeft, statusRight);
    }
  }

  private void createTracks(Map<String, Object> trackMap) {
    if (trackMap == null) {
      return;
    }
    for (Map.Entry<String, Object> track : trackMap.entrySet()) {
      int trackID = Integer.parseInt(track.getKey());
      Map<String, Integer> trackData = (Map<String, Integer>) track.getValue();
      Track t = new Track(trackData.get("numSections"));

      map.addTrack(trackID, t);
    }
  }

  private void createPlaceables(Map<String, Object> placeablesMap) {
    if (placeablesMap == null) {
      return;
    }
    for (Map.Entry<String, Object> placeable : placeablesMap.entrySet()) {
      int placeableID = Integer.parseInt(placeable.getKey());
      Map<String, Object> placeableData = (Map<String, Object>) placeable.getValue();
      Placeable p = null;
      if (placeableData.get("type").equals("fixedBalise")) {
        p = new FixedBalise(Double.valueOf((int) placeableData.get("advisorySpeed")), placeableID);
      }
      Map<String, Integer> placeOnMap = ((Map<String, Integer>) placeableData.get("placeOn"));
      int trackID = placeOnMap.get("track");
      int section = placeOnMap.get("section");

      map.addPlaceable(placeableID, p, trackID, section);
      assert (p != null) : "Placeable should not be null";
    }
  }


  private void createTrains(Map<String, Object> trains) {
    if (trains == null) {
      return;
    }
    for (Map.Entry<String, Object> train : trains.entrySet()) {
      int trainID = Integer.parseInt(train.getKey());
      Map<String, Object> trainData = (Map<String, Object>) train.getValue();
      Train t = new Train((int) trainData.get("numCars"));

      map.addTrain(trainID, t);
    }
  }
}
