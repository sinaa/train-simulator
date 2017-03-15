package ft.sim.world.connectables;

/**
 * Created by Sina on 27/02/2017.
 */
public class Station implements Connectable {

  // in meters
  private int length = 400;

  private int capacity;

  @Override
  public double getLength() {
    return length;
  }

  public Station(int capacity){
    this.capacity = capacity;
  }



}
