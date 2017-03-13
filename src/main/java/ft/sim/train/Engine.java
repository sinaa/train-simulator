package ft.sim.train;

import ft.sim.simulation.BasicSimulation;

/**
 * Created by Sina on 21/02/2017.
 */
public class Engine {

  // Which train does this engine belong to (one-to-one relationship)
  private int belongsToTrainID;

  // 201.6 km/h (56 m/s)
  // 300 km/h
  double maxSpeed = 83.33;

  // accelration in m/sec2
  double maxAcceleration = 1.0;
  double maxDeceleration = -9 * (g / 100.0);

  /*double normalAcceleration = 1.0;
  double normalDeceleration = -7 * (g / 100.0);*/
  double normalAcceleration = 0.4;
  double normalDeceleration = -0.5;

  // G-force
  public static final double g = 9.86;

  // Current speed, acceleration, targetSpeed
  double speed = 0;
  double acceleration = 0;
  double targetSpeed = 0;

  // temporary variable to store the last distance travelled
  double lastDistanceTravelled = 0;

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

  /*
   * Set the target (advisory) speed (m/s)
   */
  public void setTargetSpeed(double targetSpeed) {
    this.targetSpeed = targetSpeed;
    updateAcceleration();
  }

  /*
   * Get the target speed (m/s)
   */
  public double getTargetSpeed() {
    return targetSpeed;
  }

  /*
   * Set the acceleration of this engine
   */
  public void setAcceleration(int acceleration) {
    this.acceleration = acceleration;
  }

  /*
   * Get the current acceleration rate (m/s2)
   */
  public double getAcceleration() {
    return acceleration;
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
  void tick(double time) {
    // distance = v1 x t + 1/2 * a * t^2
    lastDistanceTravelled += speed * time + (acceleration * Math.pow(time, 2) / 2.0);

    // v2 = (t2-t1) x a + v1
    speed = time * acceleration + speed;

    if (speed < 0) {
      speed = 0;
    }
    updateAcceleration();
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
  }

  public double getLastDistanceTravelled() {
    double dist = lastDistanceTravelled;
    //totalTravelled += dist;
    lastDistanceTravelled = 0;
    return dist;
  }

  public boolean isBreaking() {
    return (acceleration > 0);
  }

  public boolean isAccelerating() {
    return (acceleration < 0);
  }

  public boolean isStill() {
    return acceleration == 0;
  }

  public boolean isStopped() {
    return isStill() && speed == 0;
  }

  /*public int belongsToTrainID() {
    return belongsToTrainID;
  }*/
}
