package ft.sim.visualisation;

/**
 * Created by sina on 16/05/2017.
 */
public class Point {

  double from = 0;
  double to = 0;
  int z = 0;

  public Point(double from, double to, int z) {
    this.from = from/2.0;
    this.to = to/2.0;
    this.z = z;
  }

  public Point(double from, double to) {
    this(from, to, 0);
  }

  @Override
  public String toString() {
    return String.format("X: %d, Y: %d, Z: %d", from, to, z);
  }
}
