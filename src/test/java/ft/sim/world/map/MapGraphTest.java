package ft.sim.world.map;

import static org.junit.Assert.*;

import ft.sim.world.connectables.Connectable;
import ft.sim.world.connectables.Track;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.junit.Test;

/**
 * Created by Sina on 03/04/2017.
 */
public class MapGraphTest {

  @Test
  public void addEdge() throws Exception {
    List<Track> trackList = new ArrayList<>();
    Track t1 = new Track(1);
    Track t2 = new Track(1);
    Track t3 = new Track(1);
    Track t4 = new Track(1);
    Track t5 = new Track(1);
    trackList.add(t1);
    trackList.add(t2);
    trackList.add(t3);
    trackList.add(t4);
    trackList.add(t5);

    MapGraph g = new MapGraph();
    Track previousTrack = null;
    for (Track t : trackList) {
      g.addEdge(previousTrack, t);
      previousTrack = t;
    }
    g.buildGraph();

    assertEquals(g.getChildren(t1).stream().findFirst().get(), t2);
    assertEquals(g.getChildren(t2).stream().findFirst().get(), t3);
    assertEquals(g.getChildren(t3).stream().findFirst().get(), t4);
    assertEquals(g.getChildren(t4).stream().findFirst().get(), t5);
    assertEquals(g.getChildren(t5).stream().map(Optional::ofNullable).findFirst()
        .flatMap(Function.identity())
        .orElse(null),null);

    assertEquals(g.getParents(t2).stream().findFirst().get(), t1);
    assertEquals(g.getParents(t3).stream().findFirst().get(), t2);
    assertEquals(g.getParents(t4).stream().findFirst().get(), t3);
    assertEquals(g.getParents(t5).stream().findFirst().get(), t4);
    assertEquals(g.getParents(t1).stream().map(Optional::ofNullable).findFirst()
        .flatMap(Function.identity())
        .orElse(null),null);

    //System.out.println(g);
  }

  @Test
  public void addEdgeCircular() throws Exception {
    List<Track> trackList = new ArrayList<>();
    Track t1 = new Track(1);
    Track t2 = new Track(1);
    Track t3 = new Track(1);
    Track t4 = new Track(1);
    Track t5 = new Track(1);
    trackList.add(t1);
    trackList.add(t2);
    trackList.add(t3);
    trackList.add(t4);
    trackList.add(t5);
    trackList.add(t1);

    MapGraph g = new MapGraph();
    Track previousTrack = null;
    for (Track t : trackList) {
      g.addEdge(previousTrack, t);
      previousTrack = t;
    }
    g.buildGraph();

    assertEquals(g.getChildren(t1).stream().findFirst().get(), t2);
    assertEquals(g.getChildren(t2).stream().findFirst().get(), t3);
    assertEquals(g.getChildren(t3).stream().findFirst().get(), t4);
    assertEquals(g.getChildren(t4).stream().findFirst().get(), t5);
    assertEquals(g.getChildren(t5).stream().map(Optional::ofNullable).findFirst()
        .flatMap(Function.identity())
        .orElse(null),t1);

    assertEquals(g.getParents(t2).stream().findFirst().get(), t1);
    assertEquals(g.getParents(t3).stream().findFirst().get(), t2);
    assertEquals(g.getParents(t4).stream().findFirst().get(), t3);
    assertEquals(g.getParents(t5).stream().findFirst().get(), t4);
    assertEquals(g.getParents(t1).stream().map(Optional::ofNullable).findFirst()
        .flatMap(Function.identity())
        .orElse(null),t5);

    //System.out.println(g);
  }

  @Test
  public void testIterator() throws Exception {
    List<Track> trackList = new ArrayList<>();
    Track t1 = new Track(1);
    Track t2 = new Track(1);
    Track t3 = new Track(1);
    Track t4 = new Track(1);
    Track t5 = new Track(1);
    trackList.add(t1);
    trackList.add(t2);
    trackList.add(t3);
    //trackList.add(t4);
    //trackList.add(t5);

    MapGraph g = new MapGraph();
    Track previousTrack = null;
    for (Track t : trackList) {
      g.addEdge(previousTrack, t);
      previousTrack = t;
    }
    g.buildGraph();

    Iterator<Connectable> iterator = g.getIterator(g.getRootConnectables().iterator().next());
    assertTrue(iterator.hasNext());
    assertEquals(t1, iterator.next());
    assertTrue(iterator.hasNext());
    assertEquals(t2, iterator.next());
    assertTrue(iterator.hasNext());
    assertEquals(t3, iterator.next());
    assertFalse(iterator.hasNext());

  }

}