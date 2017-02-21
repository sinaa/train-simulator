package ft.sim.train;

import java.util.ArrayList;

/**
 * Created by Sina on 21/02/2017.
 */
public class Train {

  ArrayList<Car> cars = null;
  int length;

  public Train(ArrayList<Car> cars) {
    this.cars = cars;
  }

  private void buildCars(int numCars){
    cars = new ArrayList<Car>(numCars);
    int length = 0;
    for (int i = 0; i < numCars; i++) {
      boolean isFirst = (i == 0);
      boolean isLast = (i == numCars - 1);
      Car c = new Car(isFirst, isLast);
      cars.add(c);
      length += c.getLength();
    }
  }

}
