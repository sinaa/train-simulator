package ft.sim.world.map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import ft.sim.train.Train;
import ft.sim.world.connectables.Connectable;
import ft.sim.world.connectables.Station;
import ft.sim.world.connectables.Switch;
import ft.sim.world.connectables.Track;
import ft.sim.world.journey.Journey;
import ft.sim.world.journey.JourneyPath;
import ft.sim.world.placeables.FixedBalise;
import ft.sim.world.placeables.Placeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.yaml.snakeyaml.Yaml;

/**
 * Created by Sina on 21/02/2017.
 */
public class GlobalMap {

  protected transient final Logger logger = LoggerFactory.getLogger(GlobalMap.class);

  private BiMap<Integer, Journey> journeysMap = HashBiMap.create();
  private BiMap<Integer, JourneyPath> journeyPathsMap = HashBiMap.create();
  private BiMap<Integer, Track> trackMap = HashBiMap.create();
  private BiMap<Integer, Placeable> placeablesMap = HashBiMap.create();
  private BiMap<Integer, Switch> switchMap = HashBiMap.create();
  private BiMap<Integer, Train> trainMap = HashBiMap.create();
  private BiMap<Integer, Station> stationMap = HashBiMap.create();

  public GlobalMap() {
    this("basic");
  }

  public GlobalMap(String mapName) {
    try {
      importBasicMap("maps/" + mapName + ".yaml");
    } catch (IOException e) {
      e.printStackTrace();
      assert false : "Failed to import map!";
    }
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
   /* for (Entry<String, Object> station : stations.entrySet()) {
      int stationID = Integer.parseInt(station.getKey());
      Map<String, Object> stationData = (Map<String, Object>) station.getValue();
      Station s = new Station((int) stationData.get("capacity"));

      addStation(stationID, s);
    }*/
  }


  private void createJourneys(Map<String, Object> journeys) {
    for (Entry<String, Object> j : journeys.entrySet()) {
      int journeyID = Integer.parseInt(j.getKey());
      Map<String, Object> journeyData = (Map<String, Object>) j.getValue();
      int trainID = (int) journeyData.get("train");
      int jpID = (int) journeyData.get("path");
      boolean isForward = (boolean) journeyData.get("isForward");

      addJourney(journeyID, jpID, trainID, isForward);
    }
  }

  private void createJourneyPaths(Map<String, Object> journeyPaths) {
    for (Entry<String, Object> journeyPath : journeyPaths.entrySet()) {
      int journeyPathID = Integer.parseInt(journeyPath.getKey());
      Map<String, Object> jData = (Map<String, Object>) journeyPath.getValue();
      List<Map<String, Object>> path = (List<Map<String, Object>>) jData.get("path");
      List<Connectable> connectables = new ArrayList<>();
      for (Map<String, Object> connectable : path) {
        int connectableID = (int) connectable.get("id");
        if (connectable.get("type").equals("track")) {
          connectables.add(getTrack(connectableID));
        } else if (connectable.get("type").equals("switch")) {
          connectables.add(getSwitch(connectableID));
        }
      }
      addJourneyPath(journeyPathID, connectables);
    }
  }

  private void createSwitches(Map<String, Object> switches) {
    for (Entry<String, Object> s : switches.entrySet()) {
      int switchID = Integer.parseInt(s.getKey());
      Map<String, Object> sData = (Map<String, Object>) s.getValue();
      List<Integer> left = (List<Integer>) sData.get("left");
      List<Integer> right = (List<Integer>) sData.get("right");

      int statusLeft = (int) sData.get("statusLeft");
      int statusRight = (int) sData.get("statusRight");

      addSwitch(switchID, left, right, statusLeft, statusRight);
    }
  }

  private void createTracks(Map<String, Object> trackMap) {
    for (Entry<String, Object> track : trackMap.entrySet()) {
      int trackID = Integer.parseInt(track.getKey());
      Map<String, Integer> trackData = (Map<String, Integer>) track.getValue();
      Track t = new Track(trackData.get("numSections"));

      addTrack(trackID, t);
    }
  }

  private void createPlaceables(Map<String, Object> placeablesMap) {
    for (Entry<String, Object> placeable : placeablesMap.entrySet()) {
      int placeableID = Integer.parseInt(placeable.getKey());
      Map<String, Object> placeableData = (Map<String, Object>) placeable.getValue();
      Placeable p = null;
      if (placeableData.get("type").equals("fixedBalise")) {
        p = new FixedBalise(Double.valueOf((int) placeableData.get("advisorySpeed")), placeableID);
      }
      Map<String, Integer> placeOnMap = ((Map<String, Integer>) placeableData.get("placeOn"));
      int trackID = placeOnMap.get("track");
      int section = placeOnMap.get("section");

      addPlaceable(placeableID, p, trackID, section);
      assert (p != null) : "Placeable should not be null";
    }
  }


  private void createTrains(Map<String, Object> trains) {
    for (Entry<String, Object> train : trains.entrySet()) {
      int trainID = Integer.parseInt(train.getKey());
      Map<String, Object> trainData = (Map<String, Object>) train.getValue();
      Train t = new Train((int) trainData.get("numCars"));

      addTrain(trainID, t);
    }
  }

  public void addTrack(int id, Track track) {
    trackMap.put(id, track);
  }

  public void addTrain(int id, Train train) {
    trainMap.put(id, train);
  }

  public void addStation(int id, Station station) {
    stationMap.put(id, station);
  }

  public void addSwitch(int id, List<Integer> left, List<Integer> right, int trackLeft,
      int trackRight) {
    List<Track> switchLeft = new ArrayList<>();
    List<Track> switchRight = new ArrayList<>();

    switchLeft.addAll(left.stream().map(t -> getTrack(t)).collect(Collectors.toList()));

    switchRight.addAll(right.stream().map(t -> getTrack(t)).collect(Collectors.toList()));

    Switch s = new Switch(switchLeft, switchRight);
    s.setStatus(getTrack(trackLeft), getTrack(trackRight));

    switchMap.put(id, s);
  }

  public void addJourney(int id, int journeyPathID, int trainID, boolean direction) {
    JourneyPath jp = getJourneyPath(journeyPathID);
    Train t = getTrain(trainID);
    Journey j = new Journey(jp, t, direction);

    journeysMap.put(id, j);
  }

  public void addJourneyPath(int id, List<Connectable> connectables) {
    JourneyPath jp = new JourneyPath(connectables);

    journeyPathsMap.put(id, jp);
  }

  private void addPlaceable(int id, Placeable p, int trackID, int sectionIndex) {
    placeablesMap.put(id, p);

    getTrack(trackID).placePlaceableOnSectionIndex(p, sectionIndex);
  }


  public int getJourneyID(Journey j) {
    return journeysMap.inverse().get(j);
  }

  public BiMap<Integer, Journey> getJourneys() {
    return journeysMap;
  }

  public Journey getJourney(int journeyID) {
    return journeysMap.get(journeyID);
  }


  public BiMap<Integer, JourneyPath> getJourneyPaths() {
    return journeyPathsMap;
  }

  public JourneyPath getJourneyPath(int journeyPathID) {
    return journeyPathsMap.get(journeyPathID);
  }

  public BiMap<Integer, Switch> getSwitches() {
    return switchMap;
  }


  public Switch getSwitch(int switchID) {
    return switchMap.get(switchID);
  }

  public int getSwitchID(Switch t) {
    return switchMap.inverse().get(t);
  }


  public BiMap<Integer, Track> getTracks() {
    return trackMap;
  }


  public Track getTrack(int trackID) {
    return trackMap.get(trackID);
  }

  public int getTrackID(Track t) {
    return trackMap.inverse().get(t);
  }


  public BiMap<Integer, Train> getTrains() {
    return trainMap;
  }


  public Train getTrain(int trainID) {
    return trainMap.get(trainID);
  }

  /*
   * Get the global ID of an unknown train
   */
  public int getTrainID(Train t) {
    return trainMap.inverse().get(t);
  }

}
