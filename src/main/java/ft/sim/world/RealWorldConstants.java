package ft.sim.world;

/**
 * Created by Sina on 04/04/2017.
 */
public class RealWorldConstants {

  // service brake distance (actual distance is 6944 meters)
  public static final int BRAKE_DISTANCE = 7200;

  // max deceleration speed (m/s^2)
  public static final double MAX_TRAIN_DECELERATION = -1.0;

  // normal deceleration speed (m/s2)
  public static final double NORMAL_TRAIN_DECELERATION = -0.5;

  // full braking (deceleration) speed (m/s2)
  public static final double FULL_TRAIN_DECELERATION = -0.7;

  // minimum braking(deceleration) speed (m/s2)
  public static final double MIN_TRAIN_DECELERATION = -0.2;

  // normal acceleration speed (m/s2)
  public static final double NORMAL_TRAIN_ACCELERATION = 0.4;

  // G-force (m/s2)
  public static final double G_FORCE = 9.86;

  // max train speed 300 km/h (m/s)
  public static final double MAX_TRAIN_SPEED = 83.33;

  // default set-off speed 48 km/h (30mph)
  public static final double DEFAULT_SET_OFF_SPEED = 13.4;

  // Rolling speed (20 mph - 8.9408 m/s)
  // 21 km/h = 6 m/s == stopping distance is 36 meters
  public static final double ROLLING_SPEED = 6;

  // Human eye can see this far (m)
  public static final int EYE_SIGHT_DISTANCE = 40;

  // Default inaccuracy rate of trains at estimating distance travelled
  public static final double TRAIN_DISTANCE_MEASUREMENT_INACCURACY_RATE = 0;

  // Train Squawk Interval (seconds)
  public static final int TRAIN_SQUAWK_INTERVAL = 10;

  // Tracks acceleration/deceleration coefficients
  public static final double DECELERATION_COEFFICIENT = 1;
  public static final double ACCELERATION_COEFFICIENT = 1;
}
