package ft.sim.world;

import static org.junit.Assert.assertEquals;

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
public class JourneyPositionBackwardTest {


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

    j = new Journey(jp, train1, false);
  }

  @Test
  public void getHeadTail() throws Exception {
    JourneyPosition jp = j.getJourneyPosition();
    double travelled = 0.01;
    //jp.update(j, travelled);

    assertEquals(j.getJourneyPath().getLength() - j.getTrain().getLength(), jp.getHeadPosition(),
        0.0001);
    assertEquals(j.getJourneyPath().getLength(), jp.getTailPosition(), 0.0001);
    Connectable lastConnectable = j.getJourneyPath().getLast();
    assertEquals(lastConnectable.getLength() - j.getTrain().getLength(),
        jp.getPositionFromFirstConnectable(), 0.00001);
  }

  @Test
  public void getSectionsOccupied() throws Exception {
    JourneyPosition jp = j.getJourneyPosition();
    double travelled = 0.01;
    jp.update(j, travelled);

    List<Connectable> journeyPath = j.getJourneyPath().getPath();
    List<Section> lastTrackSections = ((Track) journeyPath.get(2)).getSections();

    List<Section> sections = jp.getSectionsOccupied();
    //System.out.println("sections: " + sections);
    //System.out.println("sections: " + lastTrackSections);

    assertEquals(41, sections.size());
    assertEquals(sections.get(0), lastTrackSections.get(59));
    assertEquals(sections.get(40), lastTrackSections.get(99));
    assertEquals(100, lastTrackSections.size());

    jp.update(j, 1);

    assertEquals(58.99, jp.getPositionFromFirstConnectable(), 0.0000001);

    sections = jp.getSectionsOccupied();
    //System.out.println("sections: " + sections);
    assertEquals(sections.get(0), lastTrackSections.get(58));
    assertEquals(sections.get(40), lastTrackSections.get(98));
    assertEquals(41, sections.size());
  }

  @Test
  public void updatePositionSameConnectable() throws Exception {
    JourneyPosition jp = j.getJourneyPosition();
    double travelled = 8;
    jp.update(j, travelled);

    assertEquals(52, jp.getPositionFromFirstConnectable(), 0.00001);
    assertEquals(92, jp.getPositionFromLastConnectable(), 0.00001);
    //assertEquals(52, jp.getPositionFromLastConnectableEnd(), 0.00001);
    assertEquals(40, j.getTrain().getLength(), 0.00001);
    assertEquals(j.getJourneyPath().getPath().get(2), jp.getConnectablesOccupied().get(0));
    assertEquals(3, j.getJourneyPath().getPath().size());
    assertEquals(1, jp.getConnectablesOccupied().size());
  }

  @Test
  public void updatePositionOverflowToSwitch() throws Exception {
    JourneyPosition jp = j.getJourneyPosition();
    jp.update(j, 64);

    assertEquals(1, jp.getPositionFromFirstConnectable(), 0.00001);

    assertEquals(100 - 64, jp.getPositionFromLastConnectable(), 0.00001);
    assertEquals(j.getJourneyPath().getPath().get(1), jp.getConnectablesOccupied().get(0));
    assertEquals(j.getJourneyPath().getPath().get(2), jp.getConnectablesOccupied().get(1));
    assertEquals(2, jp.getConnectablesOccupied().size());
  }

  @Test
  public void updatePositionOverflowToBoth() throws Exception {
    JourneyPosition jp = j.getJourneyPosition();
    jp.update(j, 64);
    jp.update(j, 5);

    assertEquals(100 + 1 - 5, jp.getPositionFromFirstConnectable(), 0.00001);

    assertEquals(100 - 64 - 5, jp.getPositionFromLastConnectable(), 0.00001);
    assertEquals(j.getJourneyPath().getPath().get(0), jp.getConnectablesOccupied().get(0));
    assertEquals(j.getJourneyPath().getPath().get(1), jp.getConnectablesOccupied().get(1));
    assertEquals(j.getJourneyPath().getPath().get(2), jp.getConnectablesOccupied().get(2));
    assertEquals(3, jp.getConnectablesOccupied().size());
  }

  @Test
  public void updatePositionGoPastFirst() throws Exception {
    JourneyPosition jp = j.getJourneyPosition();
    jp.update(j, 64);
    jp.update(j, 5);
    jp.update(j, 32);

    assertEquals(64, jp.getPositionFromFirstConnectable(), 0.00001);

    assertEquals(4, jp.getPositionFromLastConnectable(), 0.00001);
    assertEquals(j.getJourneyPath().getPath().get(0), jp.getConnectablesOccupied().get(0));
    assertEquals(j.getJourneyPath().getPath().get(1), jp.getConnectablesOccupied().get(1));
    assertEquals(2, jp.getConnectablesOccupied().size());
  }

  @Test
  public void updatePositionGoPastBoth() throws Exception {
    JourneyPosition jp = j.getJourneyPosition();
    jp.update(j, 64);
    jp.update(j, 5);
    jp.update(j, 32);
    jp.update(j, 5);

    assertEquals(59, jp.getPositionFromFirstConnectable(), 0.00001);

    assertEquals(99, jp.getPositionFromLastConnectable(), 0.00001);
    assertEquals(j.getJourneyPath().getPath().get(0), jp.getConnectablesOccupied().get(0));
    assertEquals(1, jp.getConnectablesOccupied().size());
  }
}