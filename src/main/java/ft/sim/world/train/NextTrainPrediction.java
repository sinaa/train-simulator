package ft.sim.world.train;

import ft.sim.physics.DistanceHelper;
import ft.sim.world.RealWorldConstants;
import ft.sim.world.placeables.ActiveBaliseData;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;

/**
 * Created by sina on 19/04/2017.
 */
public class NextTrainPrediction {

  private double distance = -1;
  private ActiveBaliseData lastData;
  private ActiveBaliseData upAheadData;
  private double howFarUpAhead = 0;

  private transient Deque<ActiveBaliseData> observations = new LinkedList<>();

  public boolean anyTrainsAhead(ActiveBaliseData data) {
    if (data == null || data.getTrainSpeed() == -1) {
      return false;
    }

    return true;
  }

  public boolean anyTrainsAhead() {
    return anyTrainsAhead(lastData);
  }

  public void setUpAheadData(ActiveBaliseData upAheadData, double howFarAhead) {
    if (anyTrainsAhead(upAheadData)) {
      this.upAheadData = upAheadData;
      this.howFarUpAhead = howFarAhead;
    }
  }

  public void setLastData(ActiveBaliseData lastData) {
    if (this.lastData != null) {
      if (!Objects.equals(observations.peekFirst(), this.lastData)) {
        this.observations.addFirst(this.lastData);
      }
    }
    this.lastData = lastData;
  }

  private boolean isSameTrainUpAhead() {
    if (!anyTrainsAhead(upAheadData)) {
      return false;
    }
    return lastData.getLastTrainID() == upAheadData.getLastTrainID();
  }

  public void predict(double time, double distanceTravelledSinceLastBalise) {
    if (!anyTrainsAhead()) {
      distance = -1;
      return;
    }
    boolean sameTrainAhead = isSameTrainUpAhead();

    double timeDelta = time - lastData.getTimeLastTrainPassed();

    double potentialDistance = DistanceHelper
        .distanceTravelled(lastData.getTrainSpeed(), timeDelta);
    double worstCaseDistance = DistanceHelper
        .distanceToReachTargetSpeed(0, lastData.getTrainSpeed(),
            RealWorldConstants.MAX_TRAIN_DECELERATION);

    double acceleration = guessNextTrainAcceleration();

    if (lastData.isDecelerating() && !sameTrainAhead) {
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
    if (sameTrainAhead) {
      double distanceToStopUpAheadTrain = DistanceHelper
          .distanceToReachTargetSpeed(0, upAheadData.getTrainSpeed(),
              RealWorldConstants.MAX_TRAIN_DECELERATION);
      distanceToStopUpAheadTrain += howFarUpAhead;
      if (distance < distanceToStopUpAheadTrain) {
        distance = distanceToStopUpAheadTrain;
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
