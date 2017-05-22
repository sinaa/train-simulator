package ft.sim.world.journey;

import ft.sim.simulation.Tickable;
import ft.sim.statistics.Recordable;
import ft.sim.world.WorldHandler;
import ft.sim.world.map.GlobalMap;
import ft.sim.world.train.Train;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Sina on 27/02/2017.
 */
public class Journey implements Tickable, Recordable {

  protected static transient final Logger logger = LoggerFactory.getLogger(Journey.class);

  private double headPosition = 0;
  private double tailPosition = 0;
  private boolean directionForward = true;

  private boolean journeyStarted = false;
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

    headPosition = journeyPosition.getHeadPosition();
    tailPosition = journeyPosition.getTailPosition();

    if (!journeyStarted && totalDistanceTravelled > 0) {
      journeyStarted();
    }

    if (journeyPosition.isEnded()) {
      journeyFinished();
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

  public double getHeadPositionFromRoot() {
    return journeyPosition.getHeadPosition() + path.getDistanceFromGraphRoot();
  }

  public double getTailPositionFromRoot() {
    return journeyPosition.getTailPosition() + path.getDistanceFromGraphRoot();
  }


  public GlobalMap getWorld() {
    logger.error("Friendly advice: try not to use journey.getWorld()!");
    return WorldHandler.getWorldForJourney(this);
  }

  private void journeyStarted() {
    journeyStarted = true;
  }

  private void journeyFinished() {
    journeyFinished = true;
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

  @Override
  public int getID() {
    return train.getID();
  }
}
