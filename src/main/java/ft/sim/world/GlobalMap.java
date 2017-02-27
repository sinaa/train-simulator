package ft.sim.world;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import ft.sim.simulation.JourneyPath;
import ft.sim.train.Train;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by Sina on 21/02/2017.
 */
public class GlobalMap {

  public GlobalMap() {
    createTracks();
    createSwitches();
    createJourneyPaths();
    createTrains();
  }

  BiMap<Integer, JourneyPath> journeyPathsMap = HashBiMap.create();

  public BiMap<Integer, JourneyPath> getJourneyPaths() {
    return journeyPathsMap;
  }

  void createJourneyPaths() {
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

  BiMap<Integer, Switch> switchMap = HashBiMap.create();

  public BiMap<Integer, Switch> getSwitches() {
    return switchMap;
  }

  void createSwitches() {
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

  Switch getSwitch(int switchID) {
    return switchMap.get(switchID);
  }

  int getSwitchID(Switch t) {
    return switchMap.inverse().get(t);
  }


  BiMap<Integer, Track> trackMap = HashBiMap.create();

  public BiMap<Integer, Track> getTracks() {
    return trackMap;
  }

  void createTracks() {
    Track t1 = new Track(100);
    Track t2 = new Track(100);
    Track t3 = new Track(100);
    Track t4 = new Track(100);

    trackMap.put(1, t1);
    trackMap.put(2, t2);
    trackMap.put(3, t3);
    trackMap.put(4, t4);
  }

  Track getTrack(int trackID) {
    return trackMap.get(trackID);
  }

  int getTrackID(Track t) {
    return trackMap.inverse().get(t);
  }

  BiMap<Integer, Train> trainMap = HashBiMap.create();

  public BiMap<Integer, Train> getTrains() {
    return trainMap;
  }

  /*
     * Create all trains available in this world
     */
  void createTrains() {
    Train train1 = new Train(2);
    Train train2 = new Train(3);

    trainMap.put(1, train1);
    trainMap.put(2, train2);
  }

  Train getTrain(int trainID) {
    return trainMap.get(trainID);
  }

  /*
   * Get the global ID of an unknown train
   */
  int getTrainID(Train t) {
    return trainMap.inverse().get(t);
  }

}
