package ft.sim.world.journey;

import static ft.sim.signalling.SignalType.RED;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import ft.sim.signalling.SignalType;
import ft.sim.signalling.SignalUnit;
import ft.sim.train.Train;
import ft.sim.world.RealWorldConstants;
import ft.sim.world.connectables.Connectable;
import ft.sim.world.connectables.Observable;
import ft.sim.world.connectables.Section;
import ft.sim.world.connectables.Station;
import ft.sim.world.connectables.Track;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Sina on 06/03/2017.
 */
public class JourneyPosition {

  protected transient final Logger logger = LoggerFactory.getLogger(JourneyPosition.class);

  private JourneyPath path = null;
  private Train train = null;

  private LinkedList<Connectable> position = new LinkedList<>();
  private double positionFromFirstConnectable = 0;
  private double positionFromFirstSection = 0;

  private boolean isEnded = false;

  private boolean reachedLastConnectable = false;

  private final boolean isForward;

  public JourneyPosition(JourneyPath path, Train train, boolean isForward) {
    this(path, train, isForward, isForward ? 0 : path.getLength());
  }

  public JourneyPosition(JourneyPath path, Train train, boolean isForward, double initialPosition) {
    this.path = path;
    this.train = train;
    this.isForward = isForward;

    setInitialPosition(initialPosition);
  }

  public JourneyPosition(JourneyPath path, Train train) {
    this(path, train, 0);
  }

  public JourneyPosition(JourneyPath path, Train train, double initialPosition) {
    this(path, train, true, initialPosition);
  }

  private void setInitialPosition(double initialPosition) {
    double trainLength = train.getLength();
    double pathLength = path.getLength();

    if (pathLength < trainLength) {
      throw new IllegalArgumentException("train cannot be longer than the path.");
    }

    if (isForward) {
      position.addAll(path.getConnectablesBetween(initialPosition, trainLength));

      Connectable firstConnectable = position.peekFirst();
      double connectableStartingPosition = path.getConnectableStartingPosition(firstConnectable);
      positionFromFirstConnectable = initialPosition - connectableStartingPosition;
    } else {
      position.addAll(path.getConnectablesBetween(initialPosition, initialPosition - trainLength));

      Connectable lastConnectable = position.peekLast();
      double lastConnectableStartingPosition = path.getConnectableStartingPosition(lastConnectable);
      double lastconnectableEndingPosition =
          lastConnectableStartingPosition + lastConnectable.getLength();
      double tailPositionFromEnd = lastconnectableEndingPosition - initialPosition;

      Connectable firstConnectable = position.peekFirst();
      double firstConnectableStartingPosition = path
          .getConnectableStartingPosition(firstConnectable);

      positionFromFirstConnectable =
          lastconnectableEndingPosition - tailPositionFromEnd - trainLength
              - firstConnectableStartingPosition;

      //positionFromFirstConnectable = initialPosition - connectableStartingPosition - trainLength;
    }
  }

  public List<Connectable> getConnectablesOccupied() {
    return position;
  }

  public List<Section> getSectionsOccupied() {
    List<Section> sections = new ArrayList<>();
    double toSkip = getPositionFromFirstConnectable();
    int trainLength = train.getLength();
    double skipped = 0;
    double sectionsLength = 0;
    for (Connectable c : position) {
      if (c instanceof Track) {
        Track t = (Track) c;
        List<Section> trackSections = t.getSections();
        int sec = 0;
        for (Section s : trackSections) {
          sec++;
          // If there is still space to skip
          if (toSkip > skipped) {
            double deltaSkip = toSkip - skipped;
            // If the section is longer than the amount we need to skip
            if (s.getLength() > deltaSkip) {
              skipped += deltaSkip;
              positionFromFirstSection = deltaSkip;
              // Start the calculation of the sections length that's fitting the train
              sectionsLength += s.getLength() - deltaSkip;
              sections.add(s);
            } else {
              skipped += s.getLength();
            }
          } else {

            if (sectionsLength < trainLength) {
              sections.add(s);

              if (trainLength < sectionsLength + s.getLength()) {
                // reached the last section
                sectionsLength += trainLength - sectionsLength;
                break;
              } else {
                sectionsLength += s.getLength();
              }
            } else {
              break;
            }
          }
        }
      } else {
        skipped += c.getLength();
      }
    }

    return sections;
  }

  public double getPositionFromFirstConnectable() {
    return positionFromFirstConnectable;
  }


  public double getPositionFromLastConnectable() {
    double availableLength = getAvailableLength(positionFromFirstConnectable);

    double trainLength = train.getLength();

    double emptyLength = availableLength - trainLength;

    double lastConnectableLength = position.peekLast().getLength();

    return lastConnectableLength - emptyLength;
  }

  public double getPositionFromFirstSection() {
    return positionFromFirstSection;
  }

  public boolean isEnded() {
    return isEnded;
  }

  private double getAvailableLength(double distanceAlreadyPassed) {
    double length = 0;
    for (Connectable c : position) {
      length += c.getLength();
    }
    return length - distanceAlreadyPassed;
  }

  private transient Set<Section> coveredSections = new HashSet<>();

  public void update(Journey journey, double lastDistanceTravelled) {
    // get old sections occupied
    Set<Section> previousSections = Sets.newHashSet(getSectionsOccupied());

    // get existing connectables covered by this train
    Set<Connectable> connectablesOccupied = new HashSet<>(getConnectablesOccupied());

    // get existing stations covered by this train
    Set<Station> stationsOccupied = getConnectablesOccupied().stream()
        .filter(c -> c instanceof Station).map(c -> (Station) c).collect(Collectors.toSet());

    // update train position
    updatePosition(journey, lastDistanceTravelled);

    // get new connectables occupied by this train
    Set<Connectable> newConnectablesOccupied = new HashSet<>(getConnectablesOccupied());
    Set<Connectable> connectablesLeft = Sets.newHashSet(connectablesOccupied);
    connectablesLeft.removeAll(newConnectablesOccupied);
    // connectables train left
    if (connectablesLeft.size() > 0) {
      // TODO: do something with connectables that the train left
      connectablesLeft.forEach(c -> c.left(train));
      connectablesLeft.forEach(c -> logger.warn("{} just left {}", train, c));
    }
    // connectables train just entered
    Set<Connectable> newConnectables = Sets.newHashSet(newConnectablesOccupied);
    newConnectables.removeAll(connectablesOccupied);
    if (newConnectables.size() > 0) {
      newConnectables.forEach(c -> c.entered(train));
      newConnectables.forEach(c -> logger.warn("{} just entered {}", train, c));
    }

    // If the train is only on a station, it has now fully entered the station
    if (newConnectablesOccupied.stream().allMatch(c -> c instanceof Station)) {
      newConnectablesOccupied.stream().map(c -> (Station) c).forEach(s -> s.enteredTrain(train));
    }

    // get new stations covered by this train
    Set<Station> newStationsOccupied = getConnectablesOccupied().stream()
        .filter(c -> c instanceof Station).map(c -> (Station) c).collect(Collectors.toSet());

    // did the train leave any stations? (old station list contains stations that the train is no longer on)
    stationsOccupied.removeAll(newStationsOccupied);
    if (stationsOccupied.size() > 0) {
      // notify the sections that the train left them
      stationsOccupied.forEach(s -> s.left(train));
    }

    // get new sections occupied by the train
    List<Section> newSectionsOccupied = getSectionsOccupied();

    if(!newSectionsOccupied.isEmpty()){
      Section firstSection = newSectionsOccupied.get(0);
      firstSection.addPlaceable(train.getTrail());
    }

    // get sections the train just left
    Set<Section> sectionsTrainLeft = Sets.newHashSet(previousSections);
    sectionsTrainLeft.removeAll(newSectionsOccupied);
    // let the train know which sections it just left
    train.leftSections(sectionsTrainLeft);

    Set<Section> newSectionsOccupiedSet = Sets.newHashSet(newSectionsOccupied);
    // get the diff (only the new sections occupied)
    newSectionsOccupiedSet.removeAll(previousSections);
    // remove the old ones
    newSectionsOccupiedSet.removeAll(coveredSections);
    // let the train know which new sections it got over
    train.reachedSections(new HashSet<>(newSectionsOccupied));
    // keep track of sections the train has covered so far
    coveredSections.addAll(newSectionsOccupiedSet);

    // tell the train what it's seeing/should be seeing
    Set<Observable> observables = peek(RealWorldConstants.EYE_SIGHT_DISTANCE);
    train.see(observables);
  }

  private void updatePosition(Journey journey, double lastDistanceTravelled) {
    if (isEnded) {
      return;
    }
    if (lastDistanceTravelled == 0) {
      if (reachedLastConnectable) {
        isEnded = true;
      }
    }

    if (isForward) {
      updatePositionForward(lastDistanceTravelled);
    } else {
      updatePositionBackward(lastDistanceTravelled);
    }

  }

  private void updatePositionForward(double lastDistanceTravelled) {
    Connectable firstConnectable = position.peekFirst();
    double connectableLength = firstConnectable.getLength();
    double overflow = positionFromFirstConnectable + lastDistanceTravelled - connectableLength;

    // If we have passed the end of the first connector, remove it
    if (overflow >= 0) {
      Connectable lastConnectable = position.peekLast();
      if (lastConnectable == null) {
        logger.warn("Positions: {}", position);
        throw new NullPointerException("LastConnectable should never be empty!");
      }
      Connectable nextConnectable = path.getNext(lastConnectable);

      position.removeFirst();
      logger.debug("Removed connectable from head");
      // We removed one connectable, now we try to add new connectable at the end
      if (nextConnectable != null) {
        //position.addLast(nextConnectable);
        //logger.info("Added connectable to end");
      } else {
        reachedLastConnectable = true;
        logger.debug("On the last connectable");
      }

      if (position.isEmpty()) {
        //for some odd reason, there aren't any connectables left!
        logger.error("Position array was empty!!!");
        isEnded = true;
        return;
      }

      firstConnectable = position.peekFirst();
      positionFromFirstConnectable = overflow;

    } else {
      positionFromFirstConnectable += lastDistanceTravelled;
    }

    // If we don't have enough space for our train, add a new section to the train
    if (getAvailableLength(positionFromFirstConnectable) - train.getLength() <= 0) {
      logger.debug("Not enough space: {}, positionFromFirst: {}",
          getAvailableLength(positionFromFirstConnectable) - train.getLength(),
          positionFromFirstConnectable);

      Connectable lastConnectable = position.peekLast();
      if (lastConnectable == null) {
        logger.warn("Positions: {}", position);
        throw new NullPointerException("LastConnectable should never be empty!");
      }
      Connectable nextConnectable = path.getNext(lastConnectable);
      if (nextConnectable != null) {
        position.addLast(nextConnectable);
        logger.debug("Added connectable to end");
      } else {
        //train reached the end
        logger.debug("Train reached the end!");
        isEnded = true;
        return;
      }
    } else {
      /*logger.info(
          "Available space: {} | Available - train: {} | positionFromFirstConnectable: {} | position: {}",
          getAvailableLength(positionFromFirstConnectable),
          getAvailableLength(positionFromFirstConnectable) - train.getLength(),
          positionFromFirstConnectable, position);*/
    }
  }

  private void updatePositionBackward(double lastDistanceTravelled) {

    Connectable firstConnectable = position.peekFirst();
    double underflow = positionFromFirstConnectable - lastDistanceTravelled;

    // If we have passed the end of the first connector, remove it
    if (underflow <= 0) {
      Connectable previousConnectable = path.getPrevious(firstConnectable);

      // We removed one connectable, now we try to add new connectable at the end
      if (previousConnectable != null) {
        position.addFirst(previousConnectable);
        logger.debug("Added connectable to beginning (going backwards)");
      } else {
        logger.debug("On the first connectable (going backwards)");
        reachedLastConnectable = true;
        //isEnded = true;
        return;
      }

      if (position.isEmpty()) {
        //for some odd reason, there aren't any connectables left!
        logger.error("Position array was empty!!!");
        isEnded = true;
        return;
      }

      firstConnectable = position.peekFirst();
      // The end position minus the overflow value ( underflow is negative)
      positionFromFirstConnectable = firstConnectable.getLength() + underflow;

    } else {
      positionFromFirstConnectable -= lastDistanceTravelled;
    }

    double overflow = getPositionFromLastConnectable();
    if (overflow <= 0) {
      position.removeLast();
      logger.debug("Removed connectable from tail (going backwards)");
    }
  }

  /**
   * peek the observables in the next X meters
   */
  public Set<Observable> peek(int distance) {

    if (!isForward) {
      logger.error("!!!!! Backwards movement is not implemented !!!!!");
      //throw new IllegalStateException("backward movement is not implemented");
      return new HashSet<>();
    }

    // the reason for the +1 is because the train is past the current signal
    double headPosition = Math.ceil(getHeadPosition()) + 1;
    double to = headPosition + distance;

    if (path.getLength() < to) {
      to = path.getLength();
    }

    if (headPosition >= to) {
      logger.error("train falling from the end!");
      return new HashSet<>();
    }

    Set<Observable> x = path.getObservablesBetween(headPosition, to);
    /*if(!x.isEmpty())
      logger.warn("x:{}, from:{}, to:{}, {}", x,headPosition, to, train);*/

    return x;
  }

  public double getTailPosition() {
    return (isForward) ? getRelativeTailPosition() : getRelativeHeadPosition();
  }

  public double getHeadPosition() {
    return (isForward) ? getRelativeHeadPosition() : getRelativeTailPosition();
  }

  private double getRelativeTailPosition() {
    Connectable firstConnectable = position.peekFirst();
    return path.getConnectableStartingPosition(firstConnectable) + positionFromFirstConnectable;
  }

  private double getRelativeHeadPosition() {
    Connectable lastConnectable = position.peekLast();
    return path.getConnectableStartingPosition(lastConnectable) + getPositionFromLastConnectable();
  }


}
