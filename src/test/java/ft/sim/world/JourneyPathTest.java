package ft.sim.world;

import static org.junit.Assert.*;

import ft.sim.signalling.SignalUnit;
import ft.sim.world.connectables.Connectable;
import ft.sim.world.connectables.Observable;
import ft.sim.world.connectables.Section;
import ft.sim.world.connectables.Switch;
import ft.sim.world.connectables.Track;
import ft.sim.world.journey.JourneyPath;
import ft.sim.world.placeables.Placeable;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/**
 * Created by Sina on 07/03/2017.
 */
public class JourneyPathTest {

  @Test
  public void getConnectablesBetween() throws Exception {
    List<Connectable> journeyPath1 = new ArrayList<>();
    Track t1 = new Track(100);
    Track t2 = new Track(100);

    List<Track> switchLeft = new ArrayList<>();
    List<Track> switchRight = new ArrayList<>();

    switchLeft.add(t1);
    //switchLeft.add(getTrack(3));

    switchRight.add(t2);
    //switchRight.add(getTrack(4));

    Switch s1 = new Switch(switchLeft, switchRight, t1, t2);

    journeyPath1.add(t1);
    journeyPath1.add(s1);
    journeyPath1.add(t2);
    JourneyPath jp = new JourneyPath(journeyPath1);

    assertEquals(205, jp.getLength(), 0);

    List<Connectable> results = null;

    results = jp.getConnectablesBetween(50, 60);
    assertEquals(1, results.size());
    assertEquals(results.get(0), t1);

    results = jp.getConnectablesBetween(0, 60);
    assertEquals(1, results.size());
    assertEquals(results.get(0), t1);

    results = jp.getConnectablesBetween(0, 10);
    assertEquals(1, results.size());
    assertEquals(results.get(0), t1);

    results = jp.getConnectablesBetween(90, 101);
    assertEquals(2, results.size());
    assertEquals(results.get(0), t1);
    assertEquals(results.get(1), s1);

    results = jp.getConnectablesBetween(90, 104.999);
    assertEquals(2, results.size());
    assertEquals(results.get(0), t1);
    assertEquals(results.get(1), s1);

    results = jp.getConnectablesBetween(90, 106);
    assertEquals(3, results.size());
    assertEquals(results.get(0), t1);
    assertEquals(results.get(1), s1);
    assertEquals(results.get(2), t2);

    results = jp.getConnectablesBetween(100, 106);
    assertEquals(2, results.size());
    assertEquals(results.get(0), s1);
    assertEquals(results.get(1), t2);

    results = jp.getConnectablesBetween(100, 102);
    assertEquals(1, results.size());
    assertEquals(results.get(0), s1);
  }

  @Test
  public void getSectionsBetween() throws Exception {
    List<Connectable> journeyPath1 = new ArrayList<>();
    Track t1 = new Track(100);
    Track t2 = new Track(100);

    List<Track> switchLeft = new ArrayList<>();
    List<Track> switchRight = new ArrayList<>();

    switchLeft.add(t1);
    //switchLeft.add(getTrack(3));

    switchRight.add(t2);
    //switchRight.add(getTrack(4));

    Switch s1 = new Switch(switchLeft, switchRight, t1, t2);

    journeyPath1.add(t1);
    journeyPath1.add(s1);
    journeyPath1.add(t2);
    JourneyPath jp = new JourneyPath(journeyPath1);

    List<Section> sections = jp.getSectionsBetween(0, 2);
    List<Section> sectionsExpected = new ArrayList<>();
    sectionsExpected.add(t1.getSections().get(0));
    sectionsExpected.add(t1.getSections().get(1));
    sectionsExpected.add(t1.getSections().get(2));
    assertEquals(sectionsExpected, sections);

    sections.clear();
    sectionsExpected.clear();
    sections = jp.getSectionsBetween(0.5, 2.5);
    sectionsExpected.add(t1.getSections().get(0));
    sectionsExpected.add(t1.getSections().get(1));
    sectionsExpected.add(t1.getSections().get(2));
    assertEquals(sectionsExpected, sections);

    sections.clear();
    sectionsExpected.clear();
    sections = jp.getSectionsBetween(99.1, 100.1);
    sectionsExpected.add(t1.getSections().get(99));
    assertEquals(sectionsExpected, sections);

    sections.clear();
    sectionsExpected.clear();
    sections = jp.getSectionsBetween(99.1, 105.1);
    sectionsExpected.add(t1.getSections().get(99));
    sectionsExpected.add(t2.getSections().get(0));
    assertEquals(sectionsExpected, sections);

    sections.clear();
    sectionsExpected.clear();
    sections = jp.getSectionsBetween(99.1, 106.1);
    sectionsExpected.add(t1.getSections().get(99));
    sectionsExpected.add(t2.getSections().get(0));
    sectionsExpected.add(t2.getSections().get(1));
    assertEquals(sectionsExpected, sections);
  }

  @Test
  public void getObservablesBetween() throws Exception {
    List<Connectable> journeyPath1 = new ArrayList<>();
    Track t1 = new Track(100);
    Track t2 = new Track(100);
    Placeable p1 = new SignalUnit();
    Placeable p2 = new SignalUnit();
    t1.placePlaceableOnSectionIndex(p1, 2);
    t1.placePlaceableOnSectionIndex(p2, 20);

    List<Track> switchLeft = new ArrayList<>();
    List<Track> switchRight = new ArrayList<>();

    switchLeft.add(t1);
    //switchLeft.add(getTrack(3));

    switchRight.add(t2);
    //switchRight.add(getTrack(4));

    Switch s1 = new Switch(switchLeft, switchRight, t1, t2);

    journeyPath1.add(t1);
    journeyPath1.add(s1);
    journeyPath1.add(t2);
    JourneyPath jp = new JourneyPath(journeyPath1);

    List<Observable> observables = jp.getObservablesBetween(0, 5);
    List<Observable> observablesExpected = new ArrayList<>();
    observablesExpected.add((Observable) p1);
    assertEquals(observablesExpected, observables);

    observables.clear();
    observablesExpected.clear();
    observables = jp.getObservablesBetween(0, 30);
    observablesExpected.add((Observable) p1);
    observablesExpected.add((Observable) p2);
    assertEquals(observablesExpected, observables);
  }

}