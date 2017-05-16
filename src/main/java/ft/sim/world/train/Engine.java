package ft.sim.world.train;

import static ft.sim.world.RealWorldConstants.FULL_TRAIN_DECELERATION;
import static ft.sim.world.RealWorldConstants.MAX_TRAIN_DECELERATION;
import static ft.sim.world.RealWorldConstants.NORMAL_TRAIN_ACCELERATION;
import static ft.sim.world.RealWorldConstants.NORMAL_TRAIN_DECELERATION;
import static ft.sim.world.RealWorldConstants.ROLLING_SPEED;
import static ft.sim.world.train.TrainObjective.PROCEED;

import ft.sim.simulation.Tickable;
import ft.sim.world.RealWorldConstants;
import ft.sim.world.connectables.LineCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Sina on 21/02/2017.
 */
public class Engine implements Tickable {
  protected static final transient Logger logger = LoggerFactory.getLogger(Engine.class);

  // 201.6 km/h (56 m/s)
  // 300 km/h
  //double maxSpeed = 83.33;

  // accelration in m/sec2
  private double maxAcceleration = 1.0;
  //double maxDeceleration = -9 * (g / 100.0);
  private double maxDeceleration = MAX_TRAIN_DECELERATION;

  /*double normalAcceleration = 1.0;
  double normalDeceleration = -7 * (g / 100.0);*/
  private double normalAcceleration = NORMAL_TRAIN_ACCELERATION;
  private double normalDeceleration = NORMAL_TRAIN_DECELERATION;

  // Current speed, acceleration, targetSpeed
  private double speed = 0;
  private double acceleration = 0;
  private double targetSpeed = 0;

  // temporary variable to store the last distance travelled
  private double lastDistanceTravelled = 0;

  // What was the train told to do last time?
  private TrainObjective lastObjective = PROCEED;

  // total distance travelled
  private double totalDistanceTravelled = 0;

  // Last advisory train speed
  private double lastAdvisorySpeed = RealWorldConstants.DEFAULT_SET_OFF_SPEED;

  // a positive rate indicates an over-estimation of distance travelled,
  // a negative rate indicates an under-estimation
  private double inaccuracyRate = RealWorldConstants.TRAIN_DISTANCE_MEASUREMENT_INACCURACY_RATE;

  private LineCondition lineCondition = null;

  /*
   * Construct an engine, along with the train this engine belongs to
   */
  public Engine(Train train) {
    /*if (BasicSimulation.world != null) {
      this.belongsToTrainID = BasicSimulation.world.getTrainID(train);
    }*/
  }

  /*
   * Get current speed (m/s), m is metres
   */
  public double getSpeed() {
    return speed;
  }

  void roll() {
    setTargetSpeed(ROLLING_SPEED);
  }

  public void emergencyBreak() {
    if (speed != 0) {
      logger.warn("Emergency breaking!");
      this.targetSpeed = 0;
      acceleration = maxDeceleration;
    }
  }

  public void fullBreak() {
    if (speed != 0) {
      logger.warn("full breaking...");
      this.targetSpeed = 0;
      acceleration = FULL_TRAIN_DECELERATION;
    }
  }

  public void normalBreak() {
    if (speed != 0) {
      this.targetSpeed = 0;
      acceleration = normalDeceleration;
    }
  }

  /*
   * Get the target speed (m/s)
   */
  public double getTargetSpeed() {
    return targetSpeed;
  }

  /*
   * Set the target (advisory) speed (m/s)
   */
  public void setTargetSpeed(double targetSpeed) {
    this.targetSpeed = targetSpeed;
    updateAcceleration();
  }

  /*
   * Get the current acceleration rate (m/s2)
   */
  public double getAcceleration() {
    return acceleration;
  }

  /*
   * Set the acceleration of this engine
   */
  public void setAcceleration(int acceleration) {
    this.acceleration = acceleration;
  }

  /*
   * Set the normal acceleration rate of the engine (m/s2)
   */
  public void setAccelerationRate(double a) {
    this.normalAcceleration = a;
  }

  /*
   * Set the normal deceleration rate of the engine (m/s2)
   */
  public void setBreakingRate(double a) {
    // deceleration rate should be negative
    if (a > 0) {
      a = -a;
    }
    this.normalDeceleration = a;
  }

  /*
   * Update status given time (in seconds)
   */
  public void tick(double time) {
    // distance = v1 x t + 1/2 * a * t^2
    lastDistanceTravelled +=
        speed * time + (getRealWorldAcceleration(acceleration) * Math.pow(time, 2) / 2.0);
    totalDistanceTravelled += lastDistanceTravelled;
    // v2 = (t2-t1) x a + v1
    speed = time * getRealWorldAcceleration(acceleration) + speed;

    if (speed < 0.0001) {
      speed = 0;
    }


    updateAcceleration();
  }

  private double getRealWorldAcceleration(double acceleration) {
    if (lineCondition == null) {
      return acceleration;
    }
    if (acceleration > 0) {
      return acceleration * lineCondition.getAccelerationCoefficient();
    }
    return acceleration * lineCondition.getDecelerationCoefficient();
  }

  private void updateAcceleration() {
    double speedTargetDifference = targetSpeed - speed;

    // if within 1 m/s of the target speed, stop accelerating/decelerating
    if (targetSpeed > 0 && Math.abs(speedTargetDifference) < 0.5) {
      acceleration = 0;
    } else if (speedTargetDifference < 0 && acceleration >= 0) { // if going over target speed
      acceleration = normalDeceleration;
    } else if (speedTargetDifference > 0 && acceleration <= 0) { // if going under target speed
      acceleration = normalAcceleration;
    }

    if (speed <= 0.01 && acceleration < 0) {
      acceleration = 0;
    }


  }

  public double getLastDistanceTravelled() {
    double dist = lastDistanceTravelled;
    //totalTravelled += dist;
    lastDistanceTravelled = 0;
    return dist;
  }

  public double getTotalDistanceTravelled() {
    return totalDistanceTravelled;
  }

  public boolean isBreaking() {
    return (acceleration < 0);
  }

  public boolean isAccelerating() {
    return (acceleration > 0);
  }

  public boolean isStill() {
    return acceleration == 0;
  }

  public boolean isStopped() {
    if (speed < 0.01) {
      speed = 0;
    }
    if (acceleration < 0 & speed == 0) {
      acceleration = 0;
    }
    return isStill() && speed == 0;
  }

  public double getMaxDeceleration() {
    return maxDeceleration;
  }

  public double getNormalDeceleration() {
    return normalDeceleration;
  }

  public TrainObjective getObjective() {
    return lastObjective;
  }

  public void setObjective(TrainObjective objective) {
    this.lastObjective = objective;
  }

  public double getLastAdvisorySpeed() {
    return lastAdvisorySpeed;
  }

  public void setLastAdvisorySpeed(double lastAdvisorySpeed) {
    this.lastAdvisorySpeed = lastAdvisorySpeed;
  }

  public double getInaccuracyRate() {
    return inaccuracyRate;
  }

  public void setInaccuracyRate(double inaccuracyRate) {
    this.inaccuracyRate = inaccuracyRate;
  }

  public void setLineCondition(LineCondition lineCondition) {
    this.lineCondition = lineCondition;
  }
}
