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
