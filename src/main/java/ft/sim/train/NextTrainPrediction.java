package ft.sim.train;

import ft.sim.physics.DistanceHelper;
import ft.sim.world.RealWorldConstants;
import ft.sim.world.placeables.ActiveBaliseData;
import java.util.Deque;
import java.util.LinkedList;

/**
 * Created by sina on 19/04/2017.
 */
public class NextTrainPrediction {

  private double distance = -1;
  private ActiveBaliseData lastData;

  private Deque<ActiveBaliseData> observations = new LinkedList<>();

  private boolean anyTrainsAhead() {
    if (lastData == null || lastData.getTrainSpeed() == -1) {
      return false;
    }

    return true;
  }

  public void setLastData(ActiveBaliseData lastData) {
    if (this.lastData != null) {
      this.observations.addFirst(this.lastData);
    }
    this.lastData = lastData;
  }

  public void predict(double time, double distanceTravelledSinceLastBalise) {
    if (!anyTrainsAhead()) {
      distance = -1;
      return;
    }

    double timeDelta = time - lastData.getTimeLastTrainPassed();

    double potentialDistance = DistanceHelper
        .distanceTravelled(lastData.getTrainSpeed(), timeDelta);
    double worstCaseDistance = DistanceHelper
        .distanceToReachTargetSpeed(0, lastData.getTrainSpeed(),
            RealWorldConstants.MAX_TRAIN_DECELERATION);

    double acceleration = guessNextTrainAcceleration();

    if (lastData.isDecelerating()) {
      distance = worstCaseDistance;
    } else {
      if (acceleration > 0) {
        // best case scenario
        distance = DistanceHelper
            .distanceTravelled(lastData.getTrainSpeed(), timeDelta, acceleration);
      } else {
        distance = potentialDistance;
      }
    }
    if (distance < 0) {
      throw new IllegalStateException("distance " + distance + " cannot be negative!");
    }
    distance -= distanceTravelledSinceLastBalise;
    if (distance < 0) {
      distance = 0;
    }
  }

  public double getDistance() {
    return distance;
  }

  private double guessNextTrainAcceleration() {
    ActiveBaliseData lastObservation = observations.peekFirst();
    if (lastObservation == null) {
      return 0;
    }
    double previousSpeed = lastObservation.getTrainSpeed();
    double currentSpeed = lastData.getTrainSpeed();

    double deltaTime = lastData.getTimeLastTrainPassed() - lastObservation.getTimeLastTrainPassed();

    return DistanceHelper.getAcceleration(previousSpeed, currentSpeed, deltaTime);
  }
}
