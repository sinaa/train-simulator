package ft.sim.train;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Sina on 21/02/2017.
 */
public class Car {

  protected Logger logger = LoggerFactory.getLogger(Car.class);

  private boolean isFirst = false;
  private boolean isLast = false;
  private double length = Car.DEFAULT_LENGTH;

  // length of railway cars
  public final static transient int DEFAULT_LENGTH = 20;

  public Car() {
    this(false, false);
  }

  public Car(boolean first, boolean last) {
    this(Car.DEFAULT_LENGTH, first, last);
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

  double getLength() {
    return length;
  }
}
