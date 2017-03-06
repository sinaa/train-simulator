package ft.sim.simulation;

import ft.sim.train.Train;
import ft.sim.world.JourneyInformation;
import ft.sim.world.JourneyPath;
import ft.sim.world.JourneyPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Sina on 27/02/2017.
 */
public class Journey {

  protected Logger logger = LoggerFactory.getLogger(Journey.class);

  double headPosition = 0;
  double tailPosition = 0;
  boolean directionForward = true;

  boolean journeyFinished = false;

  private Train train;
  private JourneyPath path;
  private JourneyPosition journeyPosition;
  private JourneyInformation journeyInformation;

  public JourneyInformation getJourneyInformation() {
    return journeyInformation;
  }

  public JourneyPath getJourneyPath() {
    return path;
  }

  double trainSpeed = 0;

  private double totalDistanceTravelled = 0;

  public Journey(JourneyPath jp, Train t, boolean isForward) {
    path = jp;
    train = t;
    directionForward = isForward;
    journeyPosition = new JourneyPosition(jp, train, 0);
    journeyInformation = new JourneyInformation();
    calculateInitialPosition();
  }

  private void calculateInitialPosition() {
    double trainLength = train.getLength();
    double pathLength = path.getLength();

    if (trainLength > pathLength) {
      throw new IllegalArgumentException("A Train's length cannot be longer than the path");
    }

    if (directionForward) {
      tailPosition = 0;
      headPosition = trainLength;
    } else {
      tailPosition = pathLength;
      headPosition = pathLength - trainLength;
    }

  }

  public void tick(double time) {
    if(journeyFinished)
      return;

    train.getEngine().tick(time);
    double distanceTravelled = train.getEngine().getLastDistanceTravelled();

    journeyPosition.updatePosition(this, distanceTravelled);

    totalDistanceTravelled += distanceTravelled;

    if (directionForward) {
      headPosition += distanceTravelled;
      tailPosition += distanceTravelled;
    } else {
      headPosition -= distanceTravelled;
      tailPosition -= distanceTravelled;
    }

    double altHeadPosition = journeyPosition.getHeadPosition();
    double altTailPosition = journeyPosition.getTailPosition();

    if (altHeadPosition != headPosition && Math.abs(altHeadPosition- headPosition) > 0.0000001) {
      logger.warn("Alt head not the same | head: {}, altHead: {}", headPosition, altHeadPosition);
    }
    if (altTailPosition != tailPosition && Math.abs(altTailPosition- tailPosition) > 0.0000001) {
      logger.warn("Alt tail not the same | head: {}, altHead: {}", tailPosition, altTailPosition);
    }

    headPosition = altHeadPosition;
    tailPosition = altTailPosition;

    if(journeyPosition.isEnded()){
      journeyFinished = true;
    }
  }

  public double getTotalDistanceTravelled() {
    return totalDistanceTravelled;
  }

  public Train getTrain() {
    return train;
  }

  public JourneyPosition getJourneyPosition() {
    return journeyPosition;
  }

  @Override
  public String toString() {
    String journey = "";
    journey +=
        "train: {speed: " + train.getEngine().getSpeed() + ", acceleration: " + train.getEngine()
            .getAcceleration() + ", targetSpeed: " + train.getEngine().getTargetSpeed() + "}, ";
    journey += "journey: {headPosition: " + headPosition + ", tailPOsition: " + tailPosition
        + ", isForward:" + directionForward + "}";
    return journey;
  }
}
