package ft.sim.world.connectables;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/**
 * Created by sina on 10/04/2017.
 */
public class TrackTest {

  @Test
  public void getSections() throws Exception {
    Track t = new Track(100);

    List<Section> sections;
    List<Section> sectionsExpected = new ArrayList<>();

    sections = t.getSectionsBetween(0,1);
    sectionsExpected.add(t.getSections().get(0));
    sectionsExpected.add(t.getSections().get(1));
    assertEquals(sectionsExpected, sections);

    sectionsExpected.clear();
    sections = t.getSectionsBetween(0.1,1);
    sectionsExpected.add(t.getSections().get(0));
    sectionsExpected.add(t.getSections().get(1));
    assertEquals(sectionsExpected, sections);

    sectionsExpected.clear();
    sections = t.getSectionsBetween(0.1,1.1);
    sectionsExpected.add(t.getSections().get(0));
    sectionsExpected.add(t.getSections().get(1));
    assertEquals(sectionsExpected, sections);

    sectionsExpected.clear();
    sections = t.getSectionsBetween(0,0.5);
    sectionsExpected.add(t.getSections().get(0));
    assertEquals(sectionsExpected, sections);

    sectionsExpected.clear();
    sections = t.getSectionsBetween(99,100);
    sectionsExpected.add(t.getSections().get(99));
    assertEquals(sectionsExpected, sections);

    sectionsExpected.clear();
    sections = t.getSectionsBetween(99,99.9);
    sectionsExpected.add(t.getSections().get(99));
    assertEquals(sectionsExpected, sections);

    sectionsExpected.clear();
    sections = t.getSectionsBetween(0.5,0.8);
    sectionsExpected.add(t.getSections().get(0));
    assertEquals(sectionsExpected, sections);

    sectionsExpected.clear();
    sections = t.getSectionsBetween(0.5,1.5);
    sectionsExpected.add(t.getSections().get(0));
    sectionsExpected.add(t.getSections().get(1));
    assertEquals(sectionsExpected, sections);
  }

}