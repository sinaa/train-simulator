package ft.sim.world.map;

import static ft.sim.world.RealWorldConstants.BREAK_DISTANCE;

import com.google.common.collect.Iterables;
import ft.sim.signalling.SignalController;
import ft.sim.signalling.SignalType;
import ft.sim.signalling.SignalUnit;
import ft.sim.train.Train;
import ft.sim.train.TrainObjective;
import ft.sim.world.connectables.Connectable;
import ft.sim.world.connectables.Station;
import ft.sim.world.connectables.Track;
import ft.sim.world.journey.Journey;
import ft.sim.world.journey.JourneyPath;
import ft.sim.world.placeables.Balise;
import ft.sim.world.placeables.PassiveBalise;
import ft.sim.world.placeables.Placeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.yaml.snakeyaml.Yaml;

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
    return buildNewMap(mapName, new GlobalMap(mapName));
  }

  private static GlobalMap buildNewMap(String mapYamlFileName, GlobalMap globalMap) {
    MapBuilder mb = new MapBuilder();
    mb.map = globalMap;
    try {
      if (!mapYamlFileName.startsWith("maps/")) {
        mapYamlFileName = "maps/" + mapYamlFileName;
      }
      if (!mapYamlFileName.endsWith(".yaml")) {
        mapYamlFileName += ".yaml";
      }
      mb.importDefaults();
      mb.importBasicMap(mapYamlFileName);
      logger.info("Map {} imported successfully.", mapYamlFileName);
    } catch (IOException e) {
      logger.error("failed to import map");
      e.printStackTrace();
      throw new IllegalStateException("Failed to import map!");
    }

    mb.setupWorld();
    return mb.map;
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

    return maps;
  }

  private void importBasicMap(String mapYamlFile) throws IOException {
    Resource resource = new ClassPathResource(mapYamlFile);
    Yaml yaml = new Yaml();
    Map<String, Object> mapYaml = (Map<String, Object>) yaml.load(resource.getInputStream());

    createTracks((Map<String, Object>) mapYaml.get("tracks"));

    createStations((Map<String, Object>) mapYaml.get("stations"));

    createPlaceables((Map<String, Object>) mapYaml.get("placeables"));

    createSwitches((Map<String, Object>) mapYaml.get("switches"));

    createJourneyPaths((Map<String, Object>) mapYaml.get("journeyPaths"));

    createTrains((Map<String, Object>) mapYaml.get("trains"));

    createJourneys((Map<String, Object>) mapYaml.get("journeys"));

    setConfigurations((Map<String, Object>) mapYaml.get("simulation"));
  }

  private void importDefaults() throws IOException {
    Resource resource = new ClassPathResource("maps/defaults.yaml");
    Yaml yaml = new Yaml();
    Map<String, Object> mapYaml = (Map<String, Object>) yaml.load(resource.getInputStream());

    setConfigurations((Map<String, Object>) mapYaml.get("simulation"));
  }

  private void setupWorld() {
    setTrainsAtStations();
    buildGraph();
    setSignals();
    initBalisePositions();
  }

  private void initBalisePositions() {
    MapGraph graph = map.getGraph();
    Set<Connectable> roots = graph.getRootConnectables();
    boolean balisesInitialised = true;
    for (Connectable root : roots) {
      try {
        initBalisePosition(graph, root);
      } catch (IllegalStateException e) {
        balisesInitialised = false;
        logger.error("Map {} has branching tracks, which is not supported!", map.getName());
      }
    }
    if (balisesInitialised) {
      logger.info("Balises initliased successfully for map {}", map.getName());
    }
  }

  private void initBalisePosition(MapGraph graph, Connectable root) {
    double length = 0;
    Iterator<Connectable> mapIterator = graph.getIterator(root);
    while (mapIterator.hasNext()) {
      Connectable c = mapIterator.next();

      if (c instanceof Track) {
        List<Balise> balises = ((Track) c).getBalises();
        for (Balise b : balises) {
          int relativePosition = ((Track) c).getPlaceablePosition(b);
          b.setPosition(length + relativePosition);
        }
      }
      length += c.getLength();
    }
  }

  private void buildGraph() {
    for (JourneyPath path : map.getJourneyPaths().values()) {
      List<Connectable> connectables = path.getPath();
      Connectable previousConnectable = null;
      for (Connectable connectable : connectables) {
        map.getGraph().addEdge(previousConnectable, connectable);
        previousConnectable = connectable;
      }
    }
    map.getGraph().buildGraph();
  }

  private void setSignals() {
    if (!map.getGraph().isBuilt()) {
      throw new IllegalStateException("Cannot set signals when map isn't built");
    }
    setBlockSignals();
    setSwitchSignals();
    setStationSignals();
  }

  private void setStationSignals() {
    MapGraph graph = map.getGraph();
    Set<Station> stations = map.getStations().values();
    for (Station station : stations) {
      logger.warn("setting signals for station {}", station);
      Track nextTrack = graph.getChildren(station).stream().map(Optional::ofNullable)
          .findFirst().flatMap(Function.identity()).map(c -> (Track) c).orElse(null);
      Track previousTrack = graph.getParents(station).stream().map(Optional::ofNullable)
          .findFirst().flatMap(Function.identity()).map(c -> (Track) c).orElse(null);

      if (nextTrack != null) {
        SignalController signalControllerNext = nextTrack.getSignalController();
        if(signalControllerNext!=null)
          throw new IllegalStateException("no idea how this can happen");

        SignalController signalController = new SignalController(nextTrack);



        SignalUnit mainSignal = signalController.getMainSignal();
        nextTrack.addBlockSignal(mainSignal, 0);
        if (MapBuilderHelper.hasTrain(map, nextTrack)) {
          signalController.setStatus(SignalType.RED);
        }
        station.setNextBlockSignalController(signalController);
        nextTrack.addSignalController(signalController);
        logger.warn("added new signal controller");
      }

      if (previousTrack != null) {
        //TODO: probably install a balise so that the train slows down
      }
    }
  }

  private void setBlockSignals() {
    MapGraph graph = map.getGraph();
    Set<Connectable> roots = graph.getRootConnectables();
    if (roots.stream().iterator().next() instanceof Track) {
      logger.error("graph: {}", graph.getConnectablesGraph());
    }
    logger.warn("roots: {}", roots);
    for (Connectable root : roots) {
      Track track = graph.getFirstTrack(root);
      logger.info("root: Track-{}", map.getTrackID(track));
      addBlockSignalsOnPath(graph, track);
    }
  }

  private void addBlockSignalsOnPath(MapGraph graph, Track track) {

    if (track == null) {
      logger.info("track was null");
      return;
    }
    logger.warn("trying to add Blocksignal on track {}", map.getTrackID(((Track) track)));
    Connectable nextTrack = graph.getChildren(track).stream()
        .map(Optional::ofNullable).findFirst().flatMap(Function.identity()).orElse(null);
    if (nextTrack instanceof Track) {
      logger.warn("Next track: track {}", map.getTrackID(((Track) nextTrack)));
    }
    if (nextTrack == null) {
      logger.info("next-track was null");
      return;
    }
    if (!(nextTrack instanceof Track)) {
      graph.getChildren(nextTrack)
          .forEach(connectable -> addBlockSignalsOnPath(graph, graph.getFirstTrack(connectable)));
      logger.info("next-track was not a track");
      return;
    }
    if (!((Track) nextTrack).getBlockSignals().isEmpty()) {
      logger.info("next-track had block signals");
      return;
    }

    SignalController signalController = new SignalController(nextTrack);

    SignalUnit mainSignal = signalController.getMainSignal();
    SignalUnit distantSignal = signalController.newDistantSignal();

    if (track.getLength() > BREAK_DISTANCE) {
      int sectionIndexForDistantSignal = (int) (track.getLength() - BREAK_DISTANCE - 1);
      track.addBlockSignal(distantSignal, sectionIndexForDistantSignal);
      logger.info("added distance signal {} on section {} on track {}", distantSignal,
          sectionIndexForDistantSignal, map.getTrackID(((Track) track)));
    } else {
      throw new IllegalArgumentException("Track is not long enough for placing distant signals");
    }
    ((Track) nextTrack).addBlockSignal(mainSignal, 0);
    ((Track) nextTrack).addSignalController(signalController);

    if (MapBuilderHelper.hasTrain(map, ((Track) nextTrack))) {
      signalController.setStatus(SignalType.RED);
      logger.warn("set track-{}'s signal controller status to RED",
          map.getTrackID(((Track) nextTrack)));
    } else {
      logger.warn("track-{} doesn't have a train", map.getTrackID(((Track) nextTrack)));
    }
    addBlockSignalsOnPath(graph, (Track) nextTrack);
  }

  private void setSwitchSignals() {
    //TODO
  }

  private void setTrainsAtStations() {
    for (Journey journey : map.getJourneys().values()) {
      if (journey.isDirectionForward()) {
        Connectable firstConnectable = Iterables.getFirst(journey.getJourneyPath().getPath(), null);
        if (firstConnectable instanceof Station) {
          ((Station) firstConnectable).enteredTrain(journey.getTrain());
          journey.getTrain().setObjective(TrainObjective.STOP);
        }
      } else {
        Connectable lastConnectable = Iterables.getLast(journey.getJourneyPath().getPath(), null);
        if (lastConnectable instanceof Station) {
          ((Station) lastConnectable).enteredTrain(journey.getTrain());
          journey.getTrain().setObjective(TrainObjective.STOP);
        }
      }
    }
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
      boolean isForward = (boolean) journeyData.getOrDefault("isForward", true);

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
      map.registerSectionsForTrack(t.getSections(), trackID);
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
        p = new PassiveBalise(Double.valueOf((int) placeableData.get("advisorySpeed")),
            placeableID);
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

  private void setConfigurations(Map<String, Object> configurations) {
    if (configurations == null) {
      return;
    }
    for (Map.Entry<String, Object> config : configurations.entrySet()) {
      map.addConfiguration(config.getKey(), config.getValue());
    }
  }
}
