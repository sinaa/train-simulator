package ft.sim.train;

import static ft.sim.train.TrainObjective.*;

import ft.sim.signalling.SignalListener;
import ft.sim.signalling.SignalType;
import ft.sim.signalling.SignalUnit;
import ft.sim.simulation.Tickable;
import ft.sim.world.RealWorldConstants;
import ft.sim.world.WorldHandler;
import ft.sim.world.connectables.Observable;
import ft.sim.world.connectables.ObservableHelper;
import ft.sim.world.placeables.Balise;
import ft.sim.world.placeables.PassiveBalise;
import ft.sim.world.journey.Journey;
import ft.sim.world.placeables.Placeable;
import ft.sim.world.connectables.Section;
import ft.sim.world.placeables.ActiveBalise;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Sina on 21/02/2017.
 */
public class Train implements Tickable, SignalListener {

  protected static final transient Logger logger = LoggerFactory.getLogger(Train.class);

  private ArrayList<Car> cars = null;
  private int length;

  private Engine engine;
  private ECU ecu;

  private transient Set<Observable> observablesInSight = new HashSet<>();

  private double lastAdvisorySpeed = RealWorldConstants.DEFAULT_SET_OFF_SPEED;

  private transient Set<SignalUnit> signalsListeningTo = new HashSet<>();

  private TrainObjective lastObjective = PROCEED;

  public void startListeningTo(SignalUnit signalUnit) {
    signalsListeningTo.add(signalUnit);
    //logger.warn("started listening to {}", signalUnit);
  }

  public void stopListeningTo(SignalUnit signalUnit) {
    signalsListeningTo.remove(signalUnit);
    //logger.warn("stopped listening to {}", signalUnit);
  }

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
          if (p instanceof PassiveBalise) {
            PassiveBalise balise = (PassiveBalise) p;
            lastAdvisorySpeed = balise.getAdvisorySpeed();
            if (lastObjective == PROCEED) {
              engine.setTargetSpeed(lastAdvisorySpeed);
            }
            logger.info("{} Reached Balise, target: {}", this, lastAdvisorySpeed);
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
          if (p instanceof ActiveBalise) {
            ActiveBalise balise = (ActiveBalise) p;
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

    if (lastObjective == PROCEED && ObservableHelper.allGreen(observablesInSight)
        && engine.isStill()) {
      engine.setTargetSpeed(lastAdvisorySpeed);
    }
    if (ObservableHelper.allGreen(observablesInSight)
        && engine.getSpeed() < RealWorldConstants.ROLLING_SPEED
        && lastObjective == STOP_AND_ROLL) {
      proceedWithCaution();
    }
  }

  public void signalChange(SignalType signal) {
    switch (signal) {
      case GREEN:
        logger.warn("{} got GREEN signal! proceeding ...", this);
        engine.setTargetSpeed(lastAdvisorySpeed);
        lastObjective = PROCEED;
        break;
      case RED:
        logger.warn("{} got RED signal! stopping ...", this);
        engine.setTargetSpeed(0);
        break;
      case AMBER:
        throw new IllegalArgumentException("AMBER signal is not implemented :-)");
      default:
        throw new UnsupportedOperationException("received signalling: " + signal);
    }
  }

  public void proceedWithCaution() {
    engine.roll();
    lastObjective = PROCEED_WITH_CAUTION;
    logger.warn("proceeding with caution!");
  }

  public void see(Set<Observable> observables) {
    // handle signals
    if (!ObservableHelper.allGreen(observables)) {
      if (lastObjective != STOP && lastObjective != STOP_AND_ROLL) {
        if (ObservableHelper.hasBlockSignal(observables)) {
          engine.emergencyBreak();
          logger.error("{} Emergency breaking!", this);
          lastObjective = STOP;
        } else {
          engine.setTargetSpeed(0);
          lastObjective = STOP_AND_ROLL;
          logger.error("{} normal breaking!", this);
        }
        ObservableHelper.getRedSignals(observables).forEach(s -> s.addListener(this));
      }
    } else {
      boolean sawGreenBlockSignal = ObservableHelper.getGreenSignals(observables).stream()
          .anyMatch(s -> !s.isDistantSignal());
      if(sawGreenBlockSignal && lastObjective==PROCEED_WITH_CAUTION){
        engine.setTargetSpeed(lastAdvisorySpeed);
        logger.warn("caution no longer needed! proceeding at {}", lastAdvisorySpeed);
      }
    }

    // stop listening to observables which we passed, and start listening to the ones we see now
    /*Set<Observable> oldObservables = ObservableHelper
        .getOldObservables(observablesInSight, observables);
    ObservableHelper.getSignals(oldObservables).forEach(s -> s.stopListening(this));
    Set<Observable> newObservables = ObservableHelper
        .getNewObservables(observablesInSight, observables);
    ObservableHelper.getSignals(newObservables).forEach(s -> s.addListener(this));*/
    ObservableHelper.getSignals(observablesInSight).stream().forEach(s -> s.stopListening(this));
    ObservableHelper.getSignals(observables).stream().forEach(s -> s.addListener(this));

    observablesInSight.clear();
    observablesInSight.addAll(observables);
  }

  @Override
  public String toString() {
    try {
      return "Train-" + WorldHandler.getInstance().getWorld().getTrainID(this);
    } catch (Exception e) {
      return super.toString();
    }
  }
}
