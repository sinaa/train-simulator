package ft.sim.world.connectables;

import static ft.sim.world.signalling.SignalType.GREEN;
import static ft.sim.world.signalling.SignalType.RED;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import ft.sim.world.RealWorldConstants;
import ft.sim.world.signalling.SignalController;
import ft.sim.world.signalling.SignalLinked;
import ft.sim.world.signalling.SignalUnit;
import ft.sim.world.train.Train;
import ft.sim.world.WorldHandler;
import ft.sim.world.placeables.Balise;
import ft.sim.world.placeables.Placeable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Sina on 21/02/2017.
 */
public class Track implements Connectable, SignalLinked {

  private static final transient int DEFAULT_LENGTH = 20;
  protected static transient final Logger logger = LoggerFactory.getLogger(Track.class);
  private final ConnectableType type = ConnectableType.TRACK;
  private transient List<Section> sections;
  private int length = DEFAULT_LENGTH;
  private Set<SignalUnit> blockSignals = new HashSet<>();
  private SignalController signalController = null;
  private transient BiMap<Integer, Placeable> placeables = HashBiMap.create();

  private transient LineCondition lineCondition = new LineCondition();

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

  public void addBlockSignal(SignalUnit blockSignal, int position) {
    placePlaceableOnSectionIndex(blockSignal, position);
    blockSignals.add(blockSignal);
  }

  public Set<SignalUnit> getBlockSignals() {
    return blockSignals;
  }

  /**
   * Get sections between two positions on the track
   * NOTE: this method assumes that sections are 1 meter long
   *
   * @param from meters
   * @param to meters
   * @return list of sections
   */
  public List<Section> getSectionsBetween(double from, double to) {
    int fromIndex = (int) Math.floor(from);
    int toIndex = (int) Math.floor(to);

    if (toIndex == length) {
      toIndex--;
    }

    if (sections.size() <= toIndex) {
      throw new IllegalArgumentException("the track size " + length + " is less than " + to);
    }

    if (from > to) {
      throw new IllegalArgumentException("from > to !!");
    }

    List<Section> sectionsBetween = new ArrayList<>();
    for (int i = fromIndex; i <= toIndex; i++) {
      sectionsBetween.add(sections.get(i));
    }

    // length sanity check
    int length = sectionsBetween.stream().mapToInt(s -> s.getLength()).sum();
    double delta = to - from;
    if (length < delta || length > delta + 2) {
      throw new IllegalStateException("sections length: " + length + " | to - from: " + delta);
    }

    return sectionsBetween;
  }

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

  @Override
  public void entered(Train train) {
    logger.info("{} signal controller: {}", this, RED);
    if (signalController != null) {
      signalController.setStatus(RED);
    }
    train.enteredTrack(this);
  }

  @Override
  public void left(Train train) {
    logger.info("{} signal controller: {}", this, GREEN);
    if (signalController != null) {
      signalController.setStatus(GREEN);
    }
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

  public SignalController getSignalController() {
    return signalController;
  }

  public BiMap<Integer, Placeable> getPlaceables() {
    return placeables;
  }

  public LineCondition getLineCondition() {
    return lineCondition;
  }

  public void setLineCondition(LineCondition lineCondition) {
    this.lineCondition = lineCondition;
  }
}
