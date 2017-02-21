package ft.sim.train;

/**
 * Created by Sina on 21/02/2017.
 */
public class Car {

  private boolean isFirst = false;
  private boolean isLast = false;
  private int length = Car.DEFAULT_LENGTH;

  // length of railway cars
  public final static transient int DEFAULT_LENGTH = 20;

  public Car() {

  }

  public Car(boolean first, boolean last) {
    new Car(Car.DEFAULT_LENGTH, first, last);
  }

  public Car(int length, boolean first, boolean last) {
    this.length = length;

    this.isFirst = first;
    this.isLast = last;
  }

  boolean isFirst() {
    return isFirst;
  }

  boolean isLast() {
    return isLast;
  }

  int getLength() {
    return length;
  }
}
