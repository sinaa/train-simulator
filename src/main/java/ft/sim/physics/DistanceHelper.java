package ft.sim.physics;

/**
 * Created by sina on 19/04/2017.
 */
public class DistanceHelper {

  /**
   * Distance travelled to reach a certain speed
   */
  public static double distanceToReachTargetSpeed(double speedTarget, double speedCurrent,
      double acceleration) {
    double time = (speedTarget - speedCurrent) / acceleration;

    return distanceTravelled(speedCurrent, time, acceleration);
  }

  public static double distanceToStop(double speedCurrent, double acceleration) {
    return distanceToReachTargetSpeed(0, speedCurrent, acceleration);
  }

  public static double decelerationRateToStop(double speedCurrent, double distance) {
    // Here is how the formula is derived:
    // S = 1/2 * a * (Vf-Vi)^2/a^2  == 1/2 * (Vf-V1)^2 / a =>
    // 2 * s * a = (Vf-Vi)^2 => (-V)^2 = 2sa => a = - V^2/ 2S
    // negative because V^2 was negative before being powered by 2
    return -Math.pow(speedCurrent, 2) / (2 * distance);
  }

  /*
   * Distance travelled given average speed and time
   */
  public static double distanceTravelled(double speed, double time) {
    return speed * time;
  }

  public static double distanceTravelled(double speedCurrent, double time, double acceleration) {
    return speedCurrent * time + (acceleration * Math.pow(time, 2) / 2.0);
  }

  public static double getAcceleration(double oldSpeed, double newSpeed, double time) {
    return (newSpeed - oldSpeed) / time;
  }
}
