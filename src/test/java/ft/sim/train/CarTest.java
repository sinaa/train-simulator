package ft.sim.train;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Created by Sina on 07/03/2017.
 */
public class CarTest {

  @Test
  public void testConstructor(){
    Car c = new Car(true, false);
    assertTrue(c.isFirst());
  }

}