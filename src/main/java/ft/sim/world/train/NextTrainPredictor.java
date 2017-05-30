package ft.sim.world.train;

import static ft.sim.world.RealWorldConstants.MAX_TRAIN_DECELERATION;
import static ft.sim.world.RealWorldConstants.NORMAL_TRAIN_DECELERATION;

import ft.sim.physics.DistanceHelper;
import ft.sim.world.placeables.ActiveBaliseData;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sina on 19/04/2017.
 */
public class NextTrainPredictor {

  protected static final transient Logger logger = LoggerFactory
      .getLogger(NextTrainPredictor.class);

  private double distance = -1;
  private double worstCaseDistance = -1;
  private ActiveBaliseData lastData;
  private ActiveBaliseData upAheadData;
  private double howFarUpAhead = 0;

  private transient Deque<ActiveBaliseData> observations = new LinkedList<>();

  public boolean anyTrainsAhead(ActiveBaliseData data) {
    return (data != null && data.getTrainSpeed() != -1);
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
      return false; // If we don't know about any train ahead, assume it's the same train ahead
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

    // Potential: if the train ahead continued to travel at the speed it was going
    double potentialDistance = DistanceHelper
        .distanceTravelled(lastData.getTrainSpeed(), timeDelta);
    // Worst-Case: If the train stopped right after passing this balise
    worstCaseDistance = DistanceHelper
        .distanceToStop(lastData.getTrainSpeed(), NORMAL_TRAIN_DECELERATION);

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
          .distanceToStop(upAheadData.getTrainSpeed(), MAX_TRAIN_DECELERATION);
      distanceToStopUpAheadTrain += howFarUpAhead;
      if (distance < distanceToStopUpAheadTrain) {
        distance = distanceToStopUpAheadTrain;
      }
    }
    if (distance < 0) {
      logger.error("distance ({}) cannot be negative", distance);
      logger.warn(
          "time: {}, tDelta: {}, distanceSinceLastBalise: {}, sameTrainAhead: {}, lastData: {}",
          time, timeDelta, distanceTravelledSinceLastBalise, sameTrainAhead, lastData);
      throw new IllegalStateException("this should never happen!");
    }
    distance -= distanceTravelledSinceLastBalise;
    if (distance < 0) {
      distance = 0;
    }
  }

  public double getDistance() {
    return distance;
  }

  public double getWorstCaseDistance() {
    return worstCaseDistance;
  }

  /**
   * If we have more than one datapoint, based on the speed of the train ahead, guess
   * what the acceleration of the train is
   */
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
