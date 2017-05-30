package ft.sim.world.train;

import static ft.sim.world.RealWorldConstants.TRAIN_SQUAWK_INTERVAL;
import static ft.sim.world.train.TrainObjective.PROCEED;
import static ft.sim.world.train.TrainObjective.PROCEED_WITH_CAUTION;
import static ft.sim.world.train.TrainObjective.STOP;
import static ft.sim.world.train.TrainObjective.STOP_AND_ROLL;
import static ft.sim.world.train.TrainObjective.STOP_THEN_ROLL;

import ft.sim.simulation.Disruptable;
import ft.sim.simulation.Tickable;
import ft.sim.statistics.Recordable;
import ft.sim.statistics.StatisticsVariable;
import ft.sim.statistics.StatsHelper;
import ft.sim.world.RealWorldConstants;
import ft.sim.world.WorldHandler;
import ft.sim.world.connectables.Observable;
import ft.sim.world.connectables.ObservableHelper;
import ft.sim.world.connectables.Section;
import ft.sim.world.connectables.Station;
import ft.sim.world.connectables.Track;
import ft.sim.world.gsm.RadioSignal;
import ft.sim.world.journey.Journey;
import ft.sim.world.placeables.ActiveBalise;
import ft.sim.world.placeables.ActiveBaliseData;
import ft.sim.world.placeables.Balise;
import ft.sim.world.placeables.Obstacle;
import ft.sim.world.placeables.PassiveBalise;
import ft.sim.world.placeables.Placeable;
import ft.sim.world.signalling.SignalListener;
import ft.sim.world.signalling.SignalType;
import ft.sim.world.signalling.SignalUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Sina on 21/02/2017.
 */
public class Train implements Tickable, SignalListener, Recordable {

  protected static final transient Logger logger = LoggerFactory.getLogger(Train.class);

  private ArrayList<Car> cars = null;
  private int length;

  private Engine engine;
  private ECU ecu;

  private TrainTrail trail;

  private ActiveBaliseData otherSideData = null;

  private boolean atStation = false;

  // train ID
  private int trainID = 0;

  private transient Set<Observable> observablesInSight = new HashSet<>();


  private transient Set<SignalUnit> signalsListeningTo = new HashSet<>();


  public Train(int numCars) {
    buildCars(numCars);
    engine = new Engine(this);
    trail = new TrainTrail(this);
  }

  public Train(ArrayList<Car> cars) {
    this.cars = cars;
    engine = new Engine(this);
  }

  public void startListeningTo(SignalUnit signalUnit) {
    signalsListeningTo.add(signalUnit);
    //logger.warn("started listening to {}", signalUnit);
  }

  public void stopListeningTo(SignalUnit signalUnit) {
    signalsListeningTo.remove(signalUnit);
    //logger.warn("stopped listening to {}", signalUnit);
  }

  public void initECU(Journey journey) {
    ecu = new ECU(journey, engine);
  }

  private void buildCars(int numCars) {
    cars = new ArrayList<>(numCars);
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
        if (p instanceof Disruptable) {
          if (((Disruptable) p).isBroken()) {
            continue;
          }
        }
        if (p instanceof Balise) {
          if (p instanceof PassiveBalise) {
            PassiveBalise balise = (PassiveBalise) p;
            engine.setLastAdvisorySpeed(balise.getAdvisorySpeed());
            if (engine.getObjective() == PROCEED) {
              engine.setTargetSpeed(engine.getLastAdvisorySpeed());
              logger.info("{} New target speed: {}", this, engine.getLastAdvisorySpeed());
            }
            logger.info("{} Reached Balise, target: {}", this, engine.getLastAdvisorySpeed());
          } else if (p instanceof ActiveBalise) {
            ActiveBalise activeBalise = (ActiveBalise) p;
            ecu.updateNextTrainPrediction(activeBalise.getData());
            ecu.updateUpAheadData(activeBalise.getUpAheadData());
            if (otherSideData != null) {
              activeBalise.setUpAheadData(otherSideData);
            }

            if (activeBalise.getOtherSideData() != null) {
              otherSideData = activeBalise.getOtherSideData();
            }
          }
        } else if (p instanceof Obstacle) {
          if (((Obstacle) p).hit()) {
            crash();
          }
        }
      }
    }
  }

  public void leftSections(Set<Section> sections) {
    for (Section s : sections) {
      List<Placeable> sectionPlaceables = s.getPlaceables();
      for (Placeable p : sectionPlaceables) {
        if (p instanceof Disruptable) {
          if (((Disruptable) p).isBroken()) {
            continue;
          }
        }
        if (p instanceof Balise) {
          if (p instanceof ActiveBalise) {
            ActiveBalise balise = (ActiveBalise) p;
            balise
                .update(trainID, ecu.getTimer().getTime(), engine.getSpeed(), engine.isBraking());
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

    evaluateObjectives();

    // send OK squawk down the line
    //if (engine.getObjective() != STOP) {
    if (ecu.getTimeLastSquawkSent() + TRAIN_SQUAWK_INTERVAL < ecu.getTimer().getTime()
        && !atStation) {
      ecu.sendSquawkDownTheLine(RadioSignal.OK);
    }
    //}
  }

  private void evaluateObjectives() {
    switch (engine.getObjective()) {
      case PROCEED:
        if (engine.isStill() &&
            ObservableHelper.allGreen(observablesInSight)) {
          engine.setTargetSpeed(engine.getLastAdvisorySpeed());
        }
        break;
      case STOP_AND_ROLL:
        if (engine.getSpeed() < RealWorldConstants.ROLLING_SPEED &&
            ObservableHelper.allGreen(observablesInSight)) {
          logger.debug("{} observables: {}", this, observablesInSight);
          proceedWithCaution();
        }
        break;
      case STOP_THEN_ROLL:
        if (engine.isStopped() &&
            !ObservableHelper.anyTrains(observablesInSight) &&
            ObservableHelper.allGreen(observablesInSight)) {
          logger.debug("{} stopped and apparently no trains in sight, so proceeding...", this);
          proceedWithCaution();
        }
        break;
    }
  }

  public void signalChange(SignalType signal) {
    switch (signal) {
      case GREEN:
        logger.debug("{} got GREEN signal! proceeding ...", this);
        engine.setTargetSpeed(engine.getLastAdvisorySpeed());
        engine.setObjective(PROCEED);
        StatsHelper.logFor(StatisticsVariable.TRAIN_GOT_GREEN_SIGNAL, this);
        break;
      case RED:
        logger.debug("{} got RED signal! stopping ... (speed: {})", this, engine.getSpeed());
        engine.setTargetSpeed(0);
        StatsHelper.logFor(StatisticsVariable.TRAIN_GOT_RED_SIGNAL, this);
        break;
      case AMBER:
        throw new IllegalArgumentException("AMBER signal is not implemented :-)");
      default:
        throw new UnsupportedOperationException("received signalling: " + signal);
    }
  }

  public void proceedWithCaution() {
    if (engine.getObjective() != PROCEED_WITH_CAUTION) {
      logger.debug("{} proceeding with caution!", this);
    }
    engine.roll();
    engine.setObjective(PROCEED_WITH_CAUTION);
  }

  public void see(Set<Observable> observables) {
    // handle signals
    if (atStation) {
      return;
    }
    if (observables.size() > 0) {
      if (!ObservableHelper.allGreen(observables)) {
        if (engine.getObjective() != STOP && engine.getObjective() != STOP_AND_ROLL) {
          if (ObservableHelper.hasBlockSignal(observables)) {
            engine.fullBrake();
            logger.error("{} Full braking, for red signal!", this);
            engine.setObjective(STOP);
          } else {
            engine.setTargetSpeed(0);
            engine.setObjective(STOP_AND_ROLL);
            logger.error("{} normal braking, for red signal!", this);
          }
          ObservableHelper.getRedSignals(observables).forEach(s -> s.addListener(this));
        }
      } else {
        Set<SignalUnit> greenSignals = ObservableHelper.getGreenSignals(observables);
        if (greenSignals.size() > 0) {
          boolean sawGreenBlockSignal = greenSignals.stream().anyMatch(s -> !s.isDistantSignal());
          if (sawGreenBlockSignal && engine.getObjective() == PROCEED_WITH_CAUTION) {
            engine.setTargetSpeed(engine.getLastAdvisorySpeed());
            engine.setObjective(PROCEED);
            logger.warn("{} Saw green Block Signal. Proceeding at {}", this,
                engine.getLastAdvisorySpeed());
          }
        }
      }

      if (ObservableHelper.anyTrains(observables)) {

        if (!engine.isStopped()) {
          // give a fake estimate to the train to not start running like crazy.
          ecu.updateNextTrainPrediction(
              new ActiveBaliseData(-1, ecu.getTimer().getTime(), 1, true));
          engine.fullBrake();
          logger.debug("{} Full braking! There's a train ahead!", this);
          engine.setObjective(STOP_THEN_ROLL);
        }
        ecu.setSeeingTrainsAhead(true);
      } else {
        ecu.setSeeingTrainsAhead(false);
      }
    }

    // stop listening to observables which we passed, and start listening to the ones we see now
    /*Set<Observable> oldObservables = ObservableHelper
        .getOldObservables(observablesInSight, observables);
    ObservableHelper.getSignals(oldObservables).forEach(s -> s.stopListening(this));
    Set<Observable> newObservables = ObservableHelper
        .getNewObservables(observablesInSight, observables);
    ObservableHelper.getSignals(newObservables).forEach(s -> s.addListener(this));*/
    ObservableHelper.getSignals(observablesInSight).forEach(s -> s.stopListening(this));
    ObservableHelper.getSignals(observables).forEach(s -> s.addListener(this));

    observablesInSight.clear();
    observablesInSight.addAll(observables);
  }

  @Override
  public String toString() {
    try {
      return "Train-" + (trainID != 0 ? trainID
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

  public int getID() {
    return trainID;
  }

  public void setID(int trainID) {
    if (this.trainID != 0) {
      throw new IllegalStateException("The ID can only be set once!");
    }
    this.trainID = trainID;
  }

  public void ping(RadioSignal signal) {
    ecu.ping(signal);
  }


  /**
   * Notification event sent by a track the train just entered.
   */
  public void enteredTrack(Track track) {
    engine.setLineCondition(track.getLineCondition());
  }

  public void enteredStation(Station station) {
    ecu.sendSquawkDownTheLine(RadioSignal.AT_STATION);
    engine.fullBrake();
    engine.setObjective(STOP);
    trail.atStation();
    atStation = true;
    logger.info("{} entered {}, stopping...", this, station);
  }

  public void leftStation(Station station) {
    atStation = false;
    ecu.resetRadioTimer();
  }

  public void crash() {
    ecu.sendSquawkDownTheLine(RadioSignal.NOK);
    engine.emergencyBrake();
    engine.setObjective(STOP);
    StatsHelper.logFor(StatisticsVariable.TRAIN_CRASH, this);
  }

  public boolean isAtStation() {
    return atStation;
  }
}
