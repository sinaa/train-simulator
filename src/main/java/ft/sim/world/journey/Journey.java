package ft.sim.world.journey;

import ft.sim.simulation.Tickable;
import ft.sim.world.train.Train;
import ft.sim.world.WorldHandler;
import ft.sim.world.map.GlobalMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Sina on 27/02/2017.
 */
public class Journey implements Tickable {

  protected static transient final Logger logger = LoggerFactory.getLogger(Journey.class);

  private double headPosition = 0;
  private double tailPosition = 0;
  private boolean directionForward = true;

  private boolean journeyFinished = false;

  private Train train;
  private JourneyPath path;
  private JourneyPosition journeyPosition;
  private JourneyInformation journeyInformation;
  private JourneyTimer journeyTimer;

  private double totalDistanceTravelled = 0;

  public Journey(JourneyPath jp, Train t, boolean isForward) {
    path = jp;
    train = t;
    directionForward = isForward;
    journeyPosition = new JourneyPosition(jp, train, isForward);
    journeyInformation = new JourneyInformation();
    journeyTimer = new JourneyTimer();
    calculateInitialPosition();

    train.initECU(this);
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
    journeyTimer.tick(time);
    if (journeyFinished) {
      return;
    }

    train.tick(time);
    double distanceTravelled = train.getEngine().getLastDistanceTravelled();

    journeyPosition.update(this, distanceTravelled);

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

    if (altHeadPosition != headPosition && Math.abs(altHeadPosition - headPosition) > 0.0000001) {
      logger.warn("Alt head not the same | head: {}, altHead: {}", headPosition, altHeadPosition);
    }
    if (altTailPosition != tailPosition && Math.abs(altTailPosition - tailPosition) > 0.0000001) {
      logger.warn("Alt tail not the same | head: {}, altHead: {}", tailPosition, altTailPosition);
    }

    headPosition = altHeadPosition;
    tailPosition = altTailPosition;

    if (journeyPosition.isEnded()) {
      journeyFinished = true;
    }
  }

  public JourneyInformation getJourneyInformation() {
    return journeyInformation;
  }

  public JourneyPath getJourneyPath() {
    return path;
  }

  public boolean isJourneyFinished() {
    return journeyFinished;
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

  public double getLength() {
    return path.getLength();
  }

  public boolean isDirectionForward() {
    return directionForward;
  }

  public double getHeadPositionFromRoot(){
    return journeyPosition.getHeadPosition() + path.getDistanceFromGraphRoot();
  }

  public double getTailPositionFromRoot(){
    return journeyPosition.getTailPosition() + path.getDistanceFromGraphRoot();
  }


  public GlobalMap getWorld(){
    logger.error("Friendly advice: try not to use journey.getWorld()!");
    return WorldHandler.getWorldForJourney(this);
  }

  public JourneyTimer getJourneyTimer() {
    return journeyTimer;
  }

  @Override
  public String toString() {
    String journey = "";
    try {
      return "Journey-" + WorldHandler.getInstance().getWorld().getJourneyID(this);
    } catch (Exception e) {
      journey +=
          "train: {speed: " + train.getEngine().getSpeed() + ", acceleration: " + train.getEngine()
              .getAcceleration() + ", targetSpeed: " + train.getEngine().getTargetSpeed() + "}, ";
      journey += "journey: {headPosition: " + headPosition + ", tailPOsition: " + tailPosition
          + ", isForward:" + directionForward + "}";
      return journey;
    }
  }
}
