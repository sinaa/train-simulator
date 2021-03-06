package ft.sim.world.train;

import static ft.sim.world.RealWorldConstants.EYE_SIGHT_DISTANCE;
import static ft.sim.world.RealWorldConstants.FULL_TRAIN_DECELERATION;
import static ft.sim.world.RealWorldConstants.ROLLING_SPEED;
import static ft.sim.world.train.TrainObjective.PROCEED;
import static ft.sim.world.train.TrainObjective.STOP;
import static ft.sim.world.train.TrainObjective.STOP_AND_ROLL;
import static ft.sim.world.train.TrainObjective.STOP_THEN_ROLL;

import ft.sim.physics.DistanceHelper;
import ft.sim.simulation.Tickable;
import ft.sim.statistics.StatisticsVariable;
import ft.sim.statistics.StatsHelper;
import ft.sim.world.RealWorldConstants;
import ft.sim.world.gsm.RadioMast;
import ft.sim.world.gsm.RadioSignal;
import ft.sim.world.journey.Journey;
import ft.sim.world.journey.JourneyPlan;
import ft.sim.world.journey.JourneyTimer;
import ft.sim.world.placeables.ActiveBaliseData;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Sina on 07/03/2017.
 */
public class ECU implements Tickable {

  protected static final transient Logger logger = LoggerFactory.getLogger(ECU.class);

  // breaking distance + 50% of distance
  private static final double SAFETY_DISTANCE_MARGIN_COEFFICIENT = 0.5;

  private Engine engine;
  private transient JourneyPlan journeyPlan;
  private transient Train train;
  private JourneyTimer timer;
  private NextTrainPredictor nextTrainPredictor = new NextTrainPredictor();
  private boolean seeingTrainsAhead = false;

  private transient RadioMast radioMast;
  private RadioSignal lastRadioSignal = null;
  private double timeReceivedLastSignal = -1;
  // safe breaking distance, in meters
  private double safeBrakingDistance;
  private double totalDistanceTravelledLastBalise = 0;
  private double totalDistanceLastUpAhead = 0;
  private double lastDistanceSinceLastBalise = 0; // Only for the UI interface
  private double actualDistance = -1;
  private double timeLastSquawkSent = 0;
  private RadioSignal lastSquawkSent = RadioSignal.OK;

  public ECU(Journey journey, Engine engine) {
    this.engine = engine;
    createWorldModel(journey);
  }

  public boolean isSeeingTrainsAhead() {
    return seeingTrainsAhead;
  }

  public void setSeeingTrainsAhead(boolean seeingTrainsAhead) {
    if (this.seeingTrainsAhead == seeingTrainsAhead) {
      return;
    }
    logger.warn("{} seeing train ahead: {}", train, seeingTrainsAhead);
    this.seeingTrainsAhead = seeingTrainsAhead;
    if (seeingTrainsAhead) {
      StatsHelper.log(StatisticsVariable.TRAIN_SEEING_TRAIN_AHEAD, train);
    }
  }

  public void updateNextTrainPrediction(ActiveBaliseData lastBaliseData) {
    nextTrainPredictor.setLastData(lastBaliseData);
    totalDistanceTravelledLastBalise = engine.getTotalDistanceTravelled();
    //TODO: update prediction ?
  }

  public void updateUpAheadData(ActiveBaliseData upAhead) {
    double howFarAhead = engine.getTotalDistanceTravelled() - totalDistanceLastUpAhead;
    nextTrainPredictor.setUpAheadData(upAhead, howFarAhead);
    totalDistanceLastUpAhead = engine.getTotalDistanceTravelled();
  }

  private void createWorldModel(Journey journey) {
    this.journeyPlan = new JourneyPlan(journey);
    this.train = journey.getTrain();

    this.safeBrakingDistance = journeyPlan.getJourneyPath().getLength();
    this.timer = journey.getJourneyTimer();
  }

  public void tick(double time) {
    calculateSafeBrakingDistance(engine.getNormalDeceleration());
    if (!nextTrainPredictor.anyTrainsAhead()) {
      return;
    }

    nextTrainPredictor.predict(timer.getTime(), getEstimatedDistanceTravelledSinceLastBalise());
    double nextDistancePrediction = nextTrainPredictor.getDistance();

    // If we are seeing a train ahead, assume that it is too close
    int arbitaryCloseDistance = 5;
    if (seeingTrainsAhead && nextDistancePrediction > arbitaryCloseDistance) {
      nextDistancePrediction = arbitaryCloseDistance;
    }

    if (isTrainAheadBroken()) {
      if (engine.isStopped() && engine.getObjective() == STOP) {
        return;
      }
      if (!engine.isStopped()) {
        if (nextTrainPredictor.getWorstCaseDistance() < getStoppingDistance(
            engine.getNormalDeceleration())) {
          engine.normalBrake();
        } else if (nextTrainPredictor.getWorstCaseDistance() < getStoppingDistance(
            FULL_TRAIN_DECELERATION)) {
          engine.fullBrake();
        } else {
          if (!engine.isEmergencyBraking()) {
            StatsHelper.logFor(StatisticsVariable.TRAIN_EMERGENCY_BRAKE, train);
          }
          engine.emergencyBrake();
          //sendSquawkDownTheLine(RadioSignal.NOK);
        }
      }
      engine.setObjective(STOP);
      if (lastSquawkSent != RadioSignal.NOK) {
        sendSquawkDownTheLine(RadioSignal.NOK);
        StatsHelper.logFor(StatisticsVariable.TRAIN_AHEAD_BROKEN, train);
      }
      return;
    }

    // If we predict the next train to be within the safe breaking distance
    if (engine.getSpeed() > 1 && nextDistancePrediction >= 0
        && nextDistancePrediction < safeBrakingDistance) {
      // Do variable breaking based on how close we predict the next train to be
      if (nextDistancePrediction > getStoppingDistance(engine.getNormalDeceleration())) {
        engine.setTargetSpeed(0);
        engine.variableBrake(nextDistancePrediction);
        engine.setObjective(STOP_AND_ROLL);
        logger.debug("{} normal braking, there's a train within breaking distance {} ({}) {}",
            train, nextDistancePrediction, safeBrakingDistance,
            getStoppingDistance(engine.getNormalDeceleration()));
      } else if (nextDistancePrediction < EYE_SIGHT_DISTANCE) {
        logger.debug("{} rolling, {}, {}", nextDistancePrediction, EYE_SIGHT_DISTANCE);
        engine.setObjective(STOP_AND_ROLL);
        engine.roll();
        if (engine.getSpeed() > ROLLING_SPEED) {
          engine.fullBrake();
        }
      } else if (nextDistancePrediction < getStoppingDistance(FULL_TRAIN_DECELERATION)) {
        engine.setObjective(STOP_THEN_ROLL);
        logger.debug("{} stop then roll, {}, {}", nextDistancePrediction,
            getStoppingDistance(FULL_TRAIN_DECELERATION));
        //engine.fullBrake();
        engine.variableBrake(nextDistancePrediction);
      } else {

        if (!engine.isEmergencyBraking() && engine.getObjective() != STOP_THEN_ROLL) {
          logger.debug(
              "{} FULL braking, there's a train within braking distance {} ({}) full braking distance: {}",
              train, nextDistancePrediction, safeBrakingDistance,
              getStoppingDistance(FULL_TRAIN_DECELERATION));
          //engine.emergencyBrake();
          engine.fullBrake();
          //sendSquawkDownTheLine(RadioSignal.NOK);
          //engine.roll();
          engine.setObjective(STOP_THEN_ROLL);
        }
      }
    } else { // If train is stopped or not too close to a preceding train

      switch (engine.getObjective()) {
        case STOP_AND_ROLL:
          if (nextDistancePrediction + arbitaryCloseDistance >= safeBrakingDistance) {
            return; // This is to avoid oscillation within the safe breaking distance
          }
          engine.setObjective(PROCEED);
          break;
        case PROCEED_WITH_CAUTION:
          if (!seeingTrainsAhead) {
            logger.debug("It's probably safe to proceed [{}]", train);
            engine.setObjective(PROCEED);
          }
          break;
        case PROCEED:
          if (nextDistancePrediction >= 0 && (
              engine.getTargetSpeed() <= engine.getLastAdvisorySpeed()
                  || engine.getSpeed() > getMaxSafeSpeed())) {
            engine.setTargetSpeed(Math.min(getMaxSafeSpeed(), engine.getLastAdvisorySpeed()));
          }
          break;
      }
    }
  }

  /**
   * Get train's idea of how far it's travelled since last balise
   *
   * Use engine's setInaccuracyRate() to set the train's inaccuracy at estimating distance travelled
   *
   * @return distance travelled since last balise in meters
   */
  private double getEstimatedDistanceTravelledSinceLastBalise() {
    double realTravelled = engine.getTotalDistanceTravelled() - totalDistanceTravelledLastBalise;
    lastDistanceSinceLastBalise = realTravelled;
    double inaccuracy = engine.getInaccuracyRate() * realTravelled;
    return realTravelled + inaccuracy;
  }

  public double getStoppingDistance(double decelerationSpeed) {
    double currentSpeed = engine.getSpeed();
    double targetSpeed = 0;

    return DistanceHelper.distanceToReachTargetSpeed(targetSpeed, currentSpeed, decelerationSpeed);
  }

  private void calculateSafeBrakingDistance(double decelerationSpeed) {
    double distance = getStoppingDistance(decelerationSpeed);

    double safetyMargin = SAFETY_DISTANCE_MARGIN_COEFFICIENT * distance;

    this.safeBrakingDistance = distance + safetyMargin;
  }

  private void calculateSafeBrakingDistance() {
    calculateSafeBrakingDistance(engine.getMaxDeceleration());
  }

  private double getMaxSafeSpeed() {
    double acceleration = engine.getNormalDeceleration();
    double distance = nextTrainPredictor.getDistance();
    double maxSpeed = Math.sqrt(-2.0 * acceleration * distance);

    return maxSpeed;
  }

  public void resetRadioTimer() {
    if (lastRadioSignal == RadioSignal.OK || lastRadioSignal == RadioSignal.AT_STATION) {
      timeReceivedLastSignal = timer.getTime();
    }
  }

  JourneyTimer getTimer() {
    return timer;
  }

  private boolean isTrainAheadBroken() {
    if (lastRadioSignal == null) {
      return false;
    }
    if (lastRadioSignal == RadioSignal.AT_STATION) {
      return false;
    }
    if (lastRadioSignal == RadioSignal.NOK) {
      logger.warn("train ahead not ok for {}", train);
      return true;
    }
    if (nextTrainLikelyBroken()) {
      logger.warn("did not hear from next train, likely broken for {}", train);
      return true;
    }
    return false;
  }

  public boolean nextTrainLikelyBroken() {
    return !train.isAtStation() && (
        timeReceivedLastSignal + 2 * RealWorldConstants.TRAIN_SQUAWK_INTERVAL < timer.getTime());
  }

  public JourneyPlan getJourneyPlan() {
    return journeyPlan;
  }

  void ping(RadioSignal radioSignal) {
    timeReceivedLastSignal = timer.getTime();
    if (lastRadioSignal == RadioSignal.NOK) {
      logger.debug("{} already got an NOK radio, ignoring new {} signal.", train, radioSignal);
      return;
    }
    this.lastRadioSignal = radioSignal;
    if (lastRadioSignal == RadioSignal.NOK) {
      StatsHelper.logFor(StatisticsVariable.GSM_GOT_NOK, train);
    }
  }

  public boolean gotNOKRadio() {
    return lastRadioSignal == RadioSignal.NOK;
  }

  private Optional<RadioMast> getRadioMast() {
    return Optional.ofNullable(radioMast);
  }

  public void setRadioMast(RadioMast radioMast) {
    this.radioMast = radioMast;
  }

  public void setActualDistance(double actualDistance) {
    this.actualDistance = actualDistance;
  }

  public void sendSquawkDownTheLine(RadioSignal signal) {
    if (signal == RadioSignal.NOK) {
      logger.error("{} squawked NOK :-(", train);
    }
    if (lastSquawkSent == RadioSignal.NOK) {
      return;
    }
    lastSquawkSent = signal;
    timeLastSquawkSent = timer.getTime();
    getRadioMast().ifPresent(mast -> mast.passMessageToTrainBehind(train, signal));
  }

  public double getTimeLastSquawkSent() {
    return timeLastSquawkSent;
  }
}
