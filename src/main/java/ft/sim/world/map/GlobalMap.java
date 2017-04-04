package ft.sim.world.map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import ft.sim.train.Train;
import ft.sim.world.connectables.Connectable;
import ft.sim.world.connectables.Section;
import ft.sim.world.connectables.Station;
import ft.sim.world.connectables.Switch;
import ft.sim.world.connectables.Track;
import ft.sim.world.journey.Journey;
import ft.sim.world.journey.JourneyPath;
import ft.sim.world.placeables.Placeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private HashMap<String, Object> configurationsMap = new HashMap<>();

  private transient HashMap<Section, Integer> sectionsRegistry = new HashMap<>();

  private MapGraph graph = new MapGraph();

  public void registerSectionsForTrack(List<Section> sections, int trackID) {
    sections.forEach(section -> sectionsRegistry.put(section, trackID));
  }

  public int getTrackIDforSection(Section section) {
    return sectionsRegistry.get(section);
  }

  public MapGraph getGraph() {
    return graph;
  }

  public void addTrack(int id, Track track) {
    trackMap.put(id, track);
  }

  public void addTrain(int id, Train train) {
    trainMap.put(id, train);
  }

  public void addConfiguration(String key, Object value) {
    configurationsMap.put(key, value);
  }

  public Object getConfiguration(String key) {
    return configurationsMap.get(key);
  }

  public boolean isConfiguration(String key, Object value) {
    Object conf = configurationsMap.get(key);
    return conf != null && conf.toString().equals(value.toString());
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

    Switch s = new Switch(switchLeft, switchRight, getTrack(trackLeft), getTrack(trackRight));

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

  public void addPlaceable(int id, Placeable p, int trackID, int sectionIndex) {
    placeablesMap.put(id, p);

    getTrack(trackID).placePlaceableOnSectionIndex(p, sectionIndex);
  }

  /*
   * Journey getters
   */
  public int getJourneyID(Journey j) {
    return journeysMap.inverse().get(j);
  }

  public BiMap<Integer, Journey> getJourneys() {
    return journeysMap;
  }


  public Journey getJourney(int journeyID) {
    return journeysMap.get(journeyID);
  }

  /*
   * JourneyPath getters
   */
  public BiMap<Integer, JourneyPath> getJourneyPaths() {
    return journeyPathsMap;
  }

  public JourneyPath getJourneyPath(int journeyPathID) {
    return journeyPathsMap.get(journeyPathID);
  }

  public int getJourneyPathID(JourneyPath jp) {
    return journeyPathsMap.inverse().get(jp);
  }

  /*
   * Switch getters
   */
  public BiMap<Integer, Switch> getSwitches() {
    return switchMap;
  }


  public Switch getSwitch(int switchID) {
    return switchMap.get(switchID);
  }

  public int getSwitchID(Switch t) {
    return switchMap.inverse().get(t);
  }


  /*
   * Track getters
   */
  public BiMap<Integer, Track> getTracks() {
    return trackMap;
  }

  public Track getTrack(int trackID) {
    return trackMap.get(trackID);
  }

  public int getTrackID(Track t) {
    return trackMap.inverse().get(t);
  }


  /*
   * Train getters
   */
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


  /*
   * Station getters
   */
  public BiMap<Integer, Station> getStations() {
    return stationMap;
  }

  public Station getStation(int stationID) {
    return stationMap.get(stationID);
  }

  public int getStationID(Station s) {
    return stationMap.inverse().get(s);
  }
}
