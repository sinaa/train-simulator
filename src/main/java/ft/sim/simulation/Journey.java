package ft.sim.simulation;

import ft.sim.train.Train;
import ft.sim.world.JourneyPath;

/**
 * Created by Sina on 27/02/2017.
 */
public class Journey {

  double headPosition = 0;
  double tailPosition = 0;
  boolean directionForward = true;

  boolean journeyFinished = false;

  Train train;
  JourneyPath path;

  double trainSpeed = 0;

  public Journey(JourneyPath jp, Train t, boolean isForward) {
    path = jp;
    train = t;
    directionForward = isForward;
    calculateInitialPosition();
  }

  private void calculateInitialPosition() {
    int trainLength = train.getLength();
    int pathLength = path.getLength();

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

  public void tick(double time){
    train.getEngine().tick(time);
    double distanceTravelled = train.getEngine().getLastDistanceTravelled();

    if(directionForward){
      headPosition += distanceTravelled;
      tailPosition += distanceTravelled;
    } else {
      headPosition -= distanceTravelled;
      tailPosition -= distanceTravelled;
    }

  }

  public Train getTrain() {
    return train;
  }

  @Override
  public String toString() {
    String journey = "";
    journey += "train: {speed: " + train.getEngine().getSpeed() + ", acceleration: " + train.getEngine().getAcceleration() + ", targetSpeed: " + train.getEngine().getTargetSpeed()  + "}, ";
    journey += "journey: {headPosition: " + headPosition + ", tailPOsition: " + tailPosition + ", isForward:" + directionForward + "}";
    return journey;
  }
}
