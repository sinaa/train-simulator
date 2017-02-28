package ft.sim.world;

/**
 * Created by Sina on 27/02/2017.
 */
public class Station implements Connectable {

  // in meters
  private int length = 400;

  @Override
  public int getLength() {
    return length;
  }
}
