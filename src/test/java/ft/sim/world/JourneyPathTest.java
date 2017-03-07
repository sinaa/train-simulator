package ft.sim.world;

import static org.junit.Assert.*;

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

    Switch s1 = new Switch(switchLeft, switchRight);
    s1.setStatus(t1, t2);

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

}