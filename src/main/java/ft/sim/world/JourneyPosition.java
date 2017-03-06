package ft.sim.world;

import ft.sim.simulation.Journey;
import ft.sim.train.Train;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Sina on 06/03/2017.
 */
public class JourneyPosition {

  protected Logger logger = LoggerFactory.getLogger(JourneyPosition.class);

  private JourneyPath path = null;
  private Train train = null;

  private LinkedList<Connectable> position = new LinkedList<>();
  private double positionFromFirstConnectable = 0;
  private double positionFromFirstSection = 0;

  private boolean isEnded = false;

  public JourneyPosition(JourneyPath path, Train train) {
    new JourneyPosition(path, train, 0);
  }

  public JourneyPosition(JourneyPath path, Train train, double initialPosition) {
    this.path = path;
    this.train = train;

    setInitialPosition(initialPosition);
  }

  private void setInitialPosition(double initialPosition) {
    double trainLength = train.getLength();
    double pathLength = path.getLength();

    if (pathLength < trainLength) {
      throw new IllegalArgumentException("train cannot be longer than the path.");
    }

    position.addAll(path.getConnectablesBetween(initialPosition, trainLength));

    Connectable firstConnectable = position.peekFirst();
    double connectableStartingPosition = path.getConnectableStartingPosition(firstConnectable);
    positionFromFirstConnectable = initialPosition - connectableStartingPosition;
  }

  public List<Connectable> getConnectablesOccupied() {
    return position;
  }

  public List<Section> getSectionsOccupied() {
    List<Section> sections = new ArrayList<>();
    double toSkip = getPositionFromFirstSection();
    double trainLength = train.getLength();
    double skipped = 0;
    double sectionsLength = 0;
    for (Connectable c : position) {
      if (c instanceof Track) {
        Track t = (Track) c;
        List<Section> trackSections = t.getSections();
        for (Section s : trackSections) {
          if (skipped < toSkip) {
            if (s.getLength() > toSkip - skipped) {
              positionFromFirstSection = toSkip - skipped;
              sectionsLength += s.getLength() - positionFromFirstSection;
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

  public void updatePosition(Journey journey, double lastDistanceTravelled) {
    if (isEnded) {
      return;
    }

    if (lastDistanceTravelled == 0) {
      return;
    }

    Connectable firstConnectable = position.peekFirst();
    double connectableStartingPosition = path.getConnectableStartingPosition(firstConnectable);
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

  public double getTailPosition() {
    Connectable firstConnectable = position.peekFirst();
    return path.getConnectableStartingPosition(firstConnectable) + positionFromFirstConnectable;
  }

  public double getHeadPosition() {
    Connectable lastConnectable = position.peekLast();
    return path.getConnectableStartingPosition(lastConnectable) + getPositionFromLastConnectable();
  }

}
