package ft.sim.train;

import java.util.ArrayList;

/**
 * Created by Sina on 21/02/2017.
 */
public class Train {

  ArrayList<Car> cars = null;
  double length;

  Engine engine;

  public Train(int numCars){
    buildCars(numCars);
    engine = new Engine(this);
  }

  public Train(ArrayList<Car> cars) {
    this.cars = cars;
    engine = new Engine(this);
  }

  private void buildCars(int numCars){
    cars = new ArrayList<Car>(numCars);
    for (int i = 0; i < numCars; i++) {
      boolean isFirst = (i == 0);
      boolean isLast = (i == numCars - 1);
      Car c = new Car(isFirst, isLast);
      cars.add(c);
      length += c.getLength();
    }
  }

  public void setEngine(Engine engine){
    this.engine = engine;
  }

  public Engine getEngine() {
    return engine;
  }

  public double getLength(){
    return length;
  }

}
