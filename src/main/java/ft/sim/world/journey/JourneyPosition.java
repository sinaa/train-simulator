package ft.sim.world.journey;

import ft.sim.train.Train;
import ft.sim.world.connectables.Connectable;
import ft.sim.world.connectables.Section;
import ft.sim.world.connectables.Track;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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

  private Set<Section> coveredSections = new HashSet<>();
  private Set<Section> previousSections = new HashSet<>();

  public void update(Journey journey, double lastDistanceTravelled) {
    updatePosition(journey, lastDistanceTravelled);
    List<Section> sectionsOccupied = getSectionsOccupied();
    // get the diff
    sectionsOccupied.removeAll(previousSections);
    // remove the old ones
    sectionsOccupied.removeAll(coveredSections);

    train.reachedSections(sectionsOccupied);
    coveredSections.addAll(sectionsOccupied);
  }

  private void updatePosition(Journey journey, double lastDistanceTravelled) {
    if (isEnded || lastDistanceTravelled == 0) {
      return;
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
      logger.info("Removed connectable from head");
      // We removed one connectable, now we try to add new connectable at the end
      if (nextConnectable != null) {
        position.addLast(nextConnectable);
        logger.info("Added connectable to end");
      } else {
        logger.warn("On the last connectable");
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
      logger.info("Not enough space: {}, positionFromFirst: {}",
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
        logger.info("Added connectable to end");
      } else {
        //train reached the end
        logger.info("Train reached the end!");
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
        logger.info("Added connectable to beginning (going backwards)");
      } else {
        logger.warn("On the first connectable (going backwards)");
        isEnded = true;
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
      logger.info("Removed connectable from tail (going backwards)");
    }
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
