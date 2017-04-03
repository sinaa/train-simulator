package ft.sim.train;

import ft.sim.signalling.SignalListener;
import ft.sim.signalling.SignalType;
import ft.sim.simulation.Tickable;
import ft.sim.world.placeables.Balise;
import ft.sim.world.placeables.FixedBalise;
import ft.sim.world.journey.Journey;
import ft.sim.world.placeables.Placeable;
import ft.sim.world.connectables.Section;
import ft.sim.world.placeables.TransparentBalise;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Sina on 21/02/2017.
 */
public class Train implements Tickable, SignalListener {

  protected Logger logger = LoggerFactory.getLogger(Train.class);

  ArrayList<Car> cars = null;
  int length;

  Engine engine;
  ECU ecu;

  public Train(int numCars) {
    buildCars(numCars);
    engine = new Engine(this);
  }

  public void initECU(Journey journey) {
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

  public void reachedSections(Set<Section> sections) {
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

  public void leftSections(Set<Section> sections) {
    for (Section s : sections) {
      List<Placeable> sectionPlaceables = s.getPlaceables();
      for (Placeable p : sectionPlaceables) {
        if (p instanceof Balise) {
          if (p instanceof TransparentBalise) {
            TransparentBalise balise = (TransparentBalise) p;
            //TODO: do something with the balise
          }
        }
      }
    }
  }

  public Engine getEngine() {
    return engine;
  }

  public int getLength() {
    return length;
  }

  public void tick(double time) {
    engine.tick(time);
    ecu.tick(time);
  }

  public void signalChange(SignalType signal) {
    switch (signal) {
      case GREEN:
        //TODO: this has to be decided by the ECU
        logger.warn("TODO, green signalling");
        engine.setTargetSpeed(20);
        break;
      case RED:
        engine.setTargetSpeed(0);
        break;
      case AMBER:
        //TODO: handle amber signal?!
      default:
        throw new UnsupportedOperationException("received signalling: " + signal);
    }
  }

}
