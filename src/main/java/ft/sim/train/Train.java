package ft.sim.train;

import ft.sim.world.placeables.Balise;
import ft.sim.world.placeables.FixedBalise;
import ft.sim.world.journey.Journey;
import ft.sim.world.placeables.Placeable;
import ft.sim.world.connectables.Section;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Sina on 21/02/2017.
 */
public class Train {

  protected Logger logger = LoggerFactory.getLogger(Train.class);

  ArrayList<Car> cars = null;
  int length;

  Engine engine;
  ECU ecu;

  public Train(int numCars) {
    buildCars(numCars);
    engine = new Engine(this);
  }

  public void initECU(Journey journey){
    ecu = new ECU(journey, engine);
  }

  public Train(ArrayList<Car> cars) {
    this.cars = cars;
    engine = new Engine(this);
  }

  private void buildCars(int numCars) {
    cars = new ArrayList<Car>(numCars);
    for (int i = 0; i < numCars; i++) {
      boolean isFirst = (i == 0);
      boolean isLast = (i == numCars - 1);
      Car c = new Car(isFirst, isLast);
      cars.add(c);
      length += c.getLength();
    }
  }

  public void reachedSections(List<Section> sections) {
    for (Section s : sections) {
      List<Placeable> sectionPlaceables = s.getPlaceables();
      for (Placeable p : sectionPlaceables) {
        if (p instanceof Balise) {
          if (p instanceof FixedBalise) {
            FixedBalise balise = (FixedBalise) p;
            double advisorySpeed = balise.getAdvisorySpeed();
            engine.setTargetSpeed(advisorySpeed);
            logger.info("Reached Balise, target: {}", advisorySpeed);
          }
        }
      }
    }
  }

  public void setEngine(Engine engine) {
    this.engine = engine;
  }

  public Engine getEngine() {
    return engine;
  }

  public int getLength() {
    return length;
  }

  public void tick(double time){
    engine.tick(time);
  }

}
