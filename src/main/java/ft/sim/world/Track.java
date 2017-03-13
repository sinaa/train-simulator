package ft.sim.world;

import static java.util.stream.Collectors.toList;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.annotations.Expose;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Sina on 21/02/2017.
 */
public class Track implements Connectable {

  private final ConnectableType type = ConnectableType.TRACK;

  private transient List<Section> sections;

  private double length = DEFAULT_LENGTH;
  private static final transient double DEFAULT_LENGTH = 20;

  public Track(List<Section> sections) {
    this.sections = sections;
  }

  public Track() {
    sections = new ArrayList<Section>(20);
  }

  public Track(int numSections) {
    sections = new ArrayList<Section>(numSections);
    length = 0;
    for (int i = 0; i < numSections; i++) {
      Section trainSection = new Section();
      sections.add(trainSection);
      length += trainSection.getLength();
    }
  }

  private BiMap<Integer, Placeable> placeables = HashBiMap.create();

  protected void placePlaceableOnSectionIndex(Placeable placeable, int sectionIndex) {
    if (sectionIndex >= sections.size()) {
      throw new ArrayIndexOutOfBoundsException(
          "The section index " + sectionIndex + " does not exist. Number of sections: " +
              sections.size());
    }

    Section section = sections.get(sectionIndex);
    section.addPlaceable(placeable);

    placeables.put(sectionIndex, placeable);
  }

  public List<Balise> getBalises() {
    return placeables.values().stream().filter(t -> t instanceof Balise).map(b -> (Balise) b)
        .collect(toList());
  }

  public int getPlaceablePosition(Placeable placeable){
    return placeables.inverse().get(placeable);
  }

  public boolean hasPlaceable(Placeable placeable){
    return placeables.containsValue(placeable);
  }

  public List<Section> getSections() {
    return sections;
  }

  @Override
  public double getLength() {
    return length;
  }
}
