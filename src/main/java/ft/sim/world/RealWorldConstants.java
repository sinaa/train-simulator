package ft.sim.world;

/**
 * Created by Sina on 04/04/2017.
 */
public class RealWorldConstants {

  // service break distance (actual distance is 6944 meters)
  public static final int BREAK_DISTANCE = 7200;

  // max deceleration speed (m/s^2)
  public static final double MAX_TRAIN_DECELERATION = -1.0;

  // normal deceleration speed (m/s2)
  public static final double NORMAL_TRAIN_DECELERATION = -0.5;

  // normal acceleration speed (m/s2)
  public static final double NORMAL_TRAIN_ACCELERATION = 0.4;

  // G-force (m/s2)
  public static final double G_FORCE = 9.86;

  // max train speed 300 km/h (m/s)
  public static final double MAX_TRAIN_SPEED = 83.33;

  // default set-off speed 48 km/h (30mph)
  public static final double DEFAULT_SET_OFF_SPEED = 13.4;

  // Rolling speed (20 mph - 8.9408 m/s)
  public static final double ROLLING_SPEED = 8.9;

  // Human eye can see this far (m)
  public static final int EYE_SIGHT_DISTANCE = 40;
}
