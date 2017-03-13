package ft.sim.world;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import ft.sim.train.Train;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.yaml.snakeyaml.Yaml;

/**
 * Created by Sina on 21/02/2017.
 */
public class OriginalMapBuilder {

  private BiMap<Integer, Journey> journeysMap = HashBiMap.create();
  private BiMap<Integer, JourneyPath> journeyPathsMap = HashBiMap.create();
  private BiMap<Integer, Track> trackMap = HashBiMap.create();
  private BiMap<Integer, Placeable> placeablesMap = HashBiMap.create();
  private BiMap<Integer, Switch> switchMap = HashBiMap.create();
  private BiMap<Integer, Train> trainMap = HashBiMap.create();

  public OriginalMapBuilder() {
    createTracks();
    createPlaceables();
    createSwitches();
    createJourneyPaths();
    createTrains();
    createJourneys();
  }

  public void MapImporter() throws IOException{
    Resource resource = new ClassPathResource("maps/basic.yaml");
    Yaml yaml = new Yaml();
    Map<String, Object> map = (Map<String, Object>) yaml.load(resource.getInputStream());

    Map<Integer, Object> tracks = (Map<Integer, Object>) map.get("tracks");

    /**/
  }

  private void createJourneys() {
    JourneyPath jp1 = getJourneyPath(1);
    Train t1 = getTrain(1);
    Journey j1 = new Journey(jp1, t1, true);

    JourneyPath jp2 = getJourneyPath(2);
    Train t2 = getTrain(2);
    Journey j2 = new Journey(jp2, t2, true);

    journeysMap.put(1, j1);
    journeysMap.put(2, j2);
  }

  private void createJourneyPaths() {
    List<Connectable> journeyPath1 = new ArrayList<>();
    journeyPath1.add(getTrack(1));
    journeyPath1.add(getSwitch(1));
    journeyPath1.add(getTrack(2));
    JourneyPath j1 = new JourneyPath(journeyPath1);

    List<Connectable> journeyPath2 = new ArrayList<>();
    journeyPath2.add(getTrack(3));
    journeyPath2.add(getSwitch(1));
    journeyPath2.add(getTrack(4));
    JourneyPath j2 = new JourneyPath(journeyPath2);

    journeyPathsMap.put(1, j1);
    journeyPathsMap.put(2, j2);
  }

  private void createSwitches() {
    List<Track> switchLeft = new ArrayList<>();
    List<Track> switchRight = new ArrayList<>();

    switchLeft.add(getTrack(1));
    switchLeft.add(getTrack(3));

    switchRight.add(getTrack(2));
    switchRight.add(getTrack(4));

    Switch s1 = new Switch(switchLeft, switchRight);
    s1.setStatus(getTrack(1), getTrack(2));

    switchMap.put(1, s1);
  }

  private void createTracks() {
    Track t1 = new Track(1000);
    Track t2 = new Track(1000);
    Track t3 = new Track(1000);
    Track t4 = new Track(1000);

    trackMap.put(1, t1);
    trackMap.put(2, t2);
    trackMap.put(3, t3);
    trackMap.put(4, t4);
  }

  private void createPlaceables(){
    FixedBalise fb1 = new FixedBalise(50 ,1);
    FixedBalise fb2 = new FixedBalise(30 ,2);
    FixedBalise fb3 = new FixedBalise(10 ,3);
    FixedBalise fb4 = new FixedBalise(60 ,4);

    placeablesMap.put(1,fb1);
    placeablesMap.put(2,fb2);
    placeablesMap.put(3,fb3);
    placeablesMap.put(4,fb4);

    getTrack(1).placePlaceableOnSectionIndex(fb1, 200);
    getTrack(1).placePlaceableOnSectionIndex(fb2, 700);
    getTrack(2).placePlaceableOnSectionIndex(fb3, 0);
    getTrack(2).placePlaceableOnSectionIndex(fb4, 500);
  }

  /*
     * Create all trains available in this world
     */
  private void createTrains() {
    Train train1 = new Train(2);
    Train train2 = new Train(3);

    trainMap.put(1, train1);
    trainMap.put(2, train2);
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
