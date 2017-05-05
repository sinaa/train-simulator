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

  private TrainTrail trail;

  // train ID
  private int tID = -1;

  private transient Set<Observable> observablesInSight = new HashSet<>();


  private transient Set<SignalUnit> signalsListeningTo = new HashSet<>();


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
    trail = new TrainTrail(this);
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
            engine.setLastAdvisorySpeed(balise.getAdvisorySpeed());
            if (engine.getObjective() == PROCEED) {
              engine.setTargetSpeed(engine.getLastAdvisorySpeed());
            }
            logger.info("{} Reached Balise, target: {}", this, engine.getLastAdvisorySpeed());
          } else if (p instanceof ActiveBalise) {
            ecu.updateNextTrainPrediction(((ActiveBalise) p).getData());
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
            balise.update(tID, ecu.getTimer().getTime(), engine.getSpeed(), engine.isBreaking());
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

    if (engine.getObjective() == PROCEED && ObservableHelper.allGreen(observablesInSight)
        && engine.isStill()) {
      engine.setTargetSpeed(engine.getLastAdvisorySpeed());
    } else if (engine.getObjective() == STOP_AND_ROLL &&
        ObservableHelper.allGreen(observablesInSight) &&
        engine.getSpeed() < RealWorldConstants.ROLLING_SPEED) {
      logger.warn("observables: {}", observablesInSight);
      proceedWithCaution();
    } else if (engine.getObjective() == STOP_THEN_ROLL &&
        !ObservableHelper.anyTrains(observablesInSight) &&
        ObservableHelper.allGreen(observablesInSight) &&
        engine.isStopped()) {
      proceedWithCaution();
    }
  }

  public void signalChange(SignalType signal) {
    switch (signal) {
      case GREEN:
        logger.warn("{} got GREEN signal! proceeding ...", this);
        engine.setTargetSpeed(engine.getLastAdvisorySpeed());
        engine.setObjective(PROCEED);
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
    engine.setObjective(PROCEED_WITH_CAUTION);
    logger.warn("{} proceeding with caution!", this);
  }

  public void see(Set<Observable> observables) {
    // handle signals
    if (!ObservableHelper.allGreen(observables)) {
      if (engine.getObjective() != STOP && engine.getObjective() != STOP_AND_ROLL) {
        if (ObservableHelper.hasBlockSignal(observables)) {
          engine.emergencyBreak();
          logger.error("{} Emergency breaking!", this);
          engine.setObjective(STOP);
        } else {
          engine.setTargetSpeed(0);
          engine.setObjective(STOP_AND_ROLL);
          logger.error("{} normal breaking!", this);
        }
        ObservableHelper.getRedSignals(observables).forEach(s -> s.addListener(this));
      }
    } else {
      boolean sawGreenBlockSignal = ObservableHelper.getGreenSignals(observables).stream()
          .anyMatch(s -> !s.isDistantSignal());
      if (sawGreenBlockSignal && engine.getObjective() == PROCEED_WITH_CAUTION) {
        engine.setTargetSpeed(engine.getLastAdvisorySpeed());
        logger.warn("caution no longer needed! proceeding at {}", engine.getLastAdvisorySpeed());
      }
    }

    if (ObservableHelper.anyTrains(observables)) {
      if (!engine.isStopped()) {
        engine.emergencyBreak();
        logger.error("{} Emergency breaking! There's a train ahead!", this);
        engine.setObjective(STOP_THEN_ROLL);
      }
      ecu.setSeeingTrainsAhead(true);
    } else {
      ecu.setSeeingTrainsAhead(false);
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
      return "Train-" + ((tID != -1) ? tID
          : WorldHandler.getInstance().getWorld().getTrainID(this));
    } catch (Exception e) {
      return super.toString();
    }
  }

  public ECU getEcu() {
    return ecu;
  }

  public TrainTrail getTrail() {
    return trail;
  }

  public void setTrainID(int trainID) {
    this.tID = trainID;
  }

  public Integer getTrainID() {
    return tID;
  }
}
