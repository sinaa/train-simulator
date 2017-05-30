package ft.sim.world;

import static org.junit.Assert.*;

import ft.sim.world.train.Train;
import ft.sim.world.connectables.Connectable;
import ft.sim.world.connectables.Section;
import ft.sim.world.connectables.Switch;
import ft.sim.world.connectables.Track;
import ft.sim.world.journey.Journey;
import ft.sim.world.journey.JourneyPath;
import ft.sim.world.journey.JourneyPosition;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Sina on 07/03/2017.
 */
public class JourneyPositionTest {



  private static Journey j;

  @Before
  public void setUp() throws Exception {
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

    Train train1 = new Train(2);

    j = new Journey(jp, train1, true);
  }

  @Test
  public void getSectionsOccupied() throws Exception {
    JourneyPosition jp = j.getJourneyPosition();
    double travelled = 0.01;
    jp.update(j, travelled);

    List<Connectable> journeyPath = j.getJourneyPath().getPath();
    List<Section> firstTrackSections = ((Track)journeyPath.get(0)).getSections();

    List<Section> sections = jp.getSectionsOccupied();
    //System.out.println("sections: " + sections);
    //System.out.println("sections: " + firstTrackSections);

    assertEquals(sections.get(0), firstTrackSections.get(0));
    assertEquals(sections.get(39), firstTrackSections.get(39));
    assertEquals(41, sections.size());
    assertEquals(100, firstTrackSections.size());

    jp.update(j, 1);

    assertEquals(1.01, jp.getPositionFromFirstConnectable(), 0.0000001);

    sections = jp.getSectionsOccupied();
    //System.out.println("sections: " + sections);
    assertEquals(sections.get(0), firstTrackSections.get(1));
    assertEquals(sections.get(39), firstTrackSections.get(40));
    assertEquals(41, sections.size());
  }

  @Test
  public void updatePositionSameConnectable() throws Exception {
    JourneyPosition jp = j.getJourneyPosition();
    double travelled = 8;
    jp.update(j, travelled);

    assertEquals(8, jp.getPositionFromFirstConnectable(), 0.00001);
    assertEquals(48, jp.getPositionFromLastConnectable(), 0.00001);
    //assertEquals(52, jp.getPositionFromLastConnectableEnd(), 0.00001);
    assertEquals(40, j.getTrain().getLength(), 0.00001);
    assertEquals(jp.getConnectablesOccupied().get(0), j.getJourneyPath().getPath().get(0));
    assertEquals(3, j.getJourneyPath().getPath().size());
    assertEquals(1, jp.getConnectablesOccupied().size());
  }

  @Test
  public void updatePositionOverflowToSwitch() throws Exception {
    JourneyPosition jp = j.getJourneyPosition();
    jp.update(j, 64);

    assertEquals(64, jp.getPositionFromFirstConnectable(), 0.00001);

    assertEquals(4, jp.getPositionFromLastConnectable(), 0.00001);
    assertEquals(j.getJourneyPath().getPath().get(0), jp.getConnectablesOccupied().get(0));
    assertEquals(j.getJourneyPath().getPath().get(1), jp.getConnectablesOccupied().get(1));
    assertEquals(2, jp.getConnectablesOccupied().size());
  }

  @Test
  public void updatePositionOverflowToBoth() throws Exception {
    JourneyPosition jp = j.getJourneyPosition();
    jp.update(j, 64);
    jp.update(j, 5);

    assertEquals(69, jp.getPositionFromFirstConnectable(), 0.00001);

    assertEquals(4, jp.getPositionFromLastConnectable(), 0.00001);
    assertEquals(j.getJourneyPath().getPath().get(0), jp.getConnectablesOccupied().get(0));
    assertEquals(j.getJourneyPath().getPath().get(1), jp.getConnectablesOccupied().get(1));
    assertEquals(j.getJourneyPath().getPath().get(2), jp.getConnectablesOccupied().get(2));
    assertEquals(3, jp.getConnectablesOccupied().size());
  }

  @Test
  public void updatePositionGoPastFirst() throws Exception {
    JourneyPosition jp = j.getJourneyPosition();
    jp.update(j, 80);
    jp.update(j, 21);

    assertEquals(1, jp.getPositionFromFirstConnectable(), 0.00001);

    assertEquals(36, jp.getPositionFromLastConnectable(), 0.00001);
    assertEquals(j.getJourneyPath().getPath().get(1), jp.getConnectablesOccupied().get(0));
    assertEquals(j.getJourneyPath().getPath().get(2), jp.getConnectablesOccupied().get(1));
    assertEquals(2, jp.getConnectablesOccupied().size());
  }

  @Test
  public void updatePositionGoPastBoth() throws Exception {
    JourneyPosition jp = j.getJourneyPosition();
    jp.update(j, 80);
    jp.update(j, 21);
    jp.update(j, 10);

    assertEquals(6, jp.getPositionFromFirstConnectable(), 0.00001);

    assertEquals(46, jp.getPositionFromLastConnectable(), 0.00001);
    assertEquals(j.getJourneyPath().getPath().get(2), jp.getConnectablesOccupied().get(0));
    assertEquals(1, jp.getConnectablesOccupied().size());
  }
}