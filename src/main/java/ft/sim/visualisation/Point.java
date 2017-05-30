package ft.sim.visualisation;

/**
 * Created by sina on 16/05/2017.
 */
public class Point {

  private double from = 0;
  private double to = 0;
  private int z = 0;

  public Point(double from, double to, int z) {
    this.from = from / 2.0;
    this.to = to / 2.0;
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
