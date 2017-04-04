package ft.sim.world.connectables;

import static java.util.stream.Collectors.toList;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import ft.sim.signalling.SignalController;
import ft.sim.signalling.SignalLinked;
import ft.sim.signalling.SignalUnit;
import ft.sim.world.WorldHandler;
import ft.sim.world.placeables.Balise;
import ft.sim.world.placeables.Placeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Sina on 21/02/2017.
 */
public class Track implements Connectable, SignalLinked {

  protected transient final Logger logger = LoggerFactory.getLogger(Track.class);

  private final ConnectableType type = ConnectableType.TRACK;

  private transient List<Section> sections;

  private int length = DEFAULT_LENGTH;
  private static final transient int DEFAULT_LENGTH = 20;

  private Set<SignalUnit> blockSignals = new HashSet<>();
  private SignalController signalController = null;

  public void addBlockSignal(SignalUnit blockSignal, int position) {
    placePlaceableOnSectionIndex(blockSignal, position);
    blockSignals.add(blockSignal);
  }

  public Set<SignalUnit> getBlockSignals() {
    return blockSignals;
  }

  public Track(List<Section> sections) {
    this.sections = sections;
  }

  public Track() {
    sections = new ArrayList<Section>(20);
  }

  public Track(int numSections) {
    if (numSections == 0) {
      throw new IllegalArgumentException("Track num sections cannot be Zero.");
    }
    sections = new ArrayList<Section>(numSections);
    length = 0;
    for (int i = 0; i < numSections; i++) {
      Section trainSection = new Section();
      sections.add(trainSection);
      length += trainSection.getLength();
    }
  }

  private BiMap<Integer, Placeable> placeables = HashBiMap.create();

  public void placePlaceableOnSectionIndex(Placeable placeable, int sectionIndex) {
    if (sectionIndex >= sections.size()) {
      throw new ArrayIndexOutOfBoundsException(
          "The section index " + sectionIndex + " does not exist. Number of sections: " +
              sections.size());
    }
    Section section = sections.get(sectionIndex);
    section.addPlaceable(placeable);

    placeables.put(sectionIndex, placeable);
  }

  public int getLastSectionIndex() {
    return sections.size() - 1;
  }

  public List<Balise> getBalises() {
    return placeables.values().stream().filter(t -> t instanceof Balise).map(b -> (Balise) b)
        .collect(toList());
  }

  public int getPlaceablePosition(Placeable placeable) {
    return placeables.inverse().get(placeable);
  }

  public boolean hasPlaceable(Placeable placeable) {
    return placeables.containsValue(placeable);
  }

  public List<Section> getSections() {
    return sections;
  }

  @Override
  public double getLength() {
    return length;
  }

  public int getSectionPosition(Section section) {
    return sections.indexOf(section);
  }

  @Override
  public String toString() {
    try {
      return "Track-" + WorldHandler.getInstance().getWorld().getTrackID(this);
    } catch (Exception e) {
      return super.toString();
    }
  }

  @Override
  public void addSignalController(SignalController signalController) {
    this.signalController = signalController;
  }
}
