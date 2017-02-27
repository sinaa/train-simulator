package ft.sim.world;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sina on 21/02/2017.
 */
public class Track implements Connectable {

  List<Section> sections;

  int length = DEFAULT_LENGTH;
  public static final transient int DEFAULT_LENGTH = 20;

  public Track() {
    sections = new ArrayList<Section>(20);
  }

  public Track(int numSections) {
    sections = new ArrayList<Section>(numSections);
    for (int i = 0; i < numSections; i++) {
      Section trainSection = new Section();
      sections.add(trainSection);
      length += trainSection.length;
    }
  }

  public Track(List<Section> sections) {
    this.sections = sections;
  }

  @Override
  public int getLength() {
    return length;
  }
}
