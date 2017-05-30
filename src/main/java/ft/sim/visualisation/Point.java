package ft.sim.visualisation;

/**
 * Created by sina on 16/05/2017.
 */
public class Point {

  // Scale down the track by a factor
  private static final double SCALE_DOWN_FACTOR = 2.0;

  private double from = 0;
  private double to = 0;
  private int z = 0;

  public Point(double from, double to, int z) {
    this.from = from / SCALE_DOWN_FACTOR;
    this.to = to / SCALE_DOWN_FACTOR;
    this.z = z;
  }

  public Point(double from, double to) {
    this(from, to, 0);
  }

  @Override
  public String toString() {
    return String.format("X: %.2f, Y: %.2f, Z: %d", from, to, z);
  }
}
