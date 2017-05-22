package ft.sim.world.train;

import static ft.sim.world.RealWorldConstants.EYE_SIGHT_DISTANCE;
import static ft.sim.world.RealWorldConstants.FULL_TRAIN_DECELERATION;
import static ft.sim.world.RealWorldConstants.ROLLING_SPEED;
import static ft.sim.world.train.TrainObjective.PROCEED;
import static ft.sim.world.train.TrainObjective.PROCEED_WITH_CAUTION;
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
  private double safeBreakingDistance;
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

    this.safeBreakingDistance = journeyPlan.getJourneyPath().getLength();
    this.timer = journey.getJourneyTimer();
  }

  public void tick(double time) {
    calculateSafeBreakingDistance(engine.getNormalDeceleration());
    if (!nextTrainPredictor.anyTrainsAhead()) {
      return;
    }

    nextTrainPredictor.predict(timer.getTime(), getEstimatedDistanceTravelledSinceLastBalise());
    double nextDistancePrediction = nextTrainPredictor.getDistance();

    int arbitaryCloseDistance = 5;
    if (seeingTrainsAhead && nextDistancePrediction > arbitaryCloseDistance) {
      nextDistancePrediction = arbitaryCloseDistance;
    }

    if (isTrainAheadBroken()) {
      if (nextTrainPredictor.getWorstCaseDistance() < calculateBreakingDistance(
          engine.getNormalDeceleration())) {
        engine.normalBreak();
      } else if (nextTrainPredictor.getWorstCaseDistance() < calculateBreakingDistance(
          FULL_TRAIN_DECELERATION)) {
        engine.fullBreak();
      } else {
        if (!engine.isEmergencyBreaking()) {
          StatsHelper.logFor(StatisticsVariable.TRAIN_EMERGENCY_BREAK, train);
        }
        engine.emergencyBreak();
        //sendSquawkDownTheLine(RadioSignal.NOK);
      }
      engine.setObjective(STOP_THEN_ROLL);
      sendSquawkDownTheLine(RadioSignal.NOK);
      StatsHelper.logFor(StatisticsVariable.TRAIN_AHEAD_BROKEN, train);
      return;
    }
    /*if (nextDistancePrediction != -1) {
      logger.info("[{}] Next train is {} meters away. safe distance: {}", train,
          nextDistancePrediction, safeBreakingDistance);
    }*/
    if (engine.getSpeed() > 1 && nextDistancePrediction >= 0
        && nextDistancePrediction < safeBreakingDistance) {
      if (nextDistancePrediction > calculateBreakingDistance(engine.getNormalDeceleration())) {
        engine.setTargetSpeed(0);
        engine.setObjective(STOP_AND_ROLL);
        logger.debug("{} normal breaking, there's a train within breaking distance {} ({}) {}",
            train, nextDistancePrediction, safeBreakingDistance,
            calculateBreakingDistance(engine.getNormalDeceleration()));
      } else if (nextDistancePrediction < EYE_SIGHT_DISTANCE) {
        logger.debug("{} rolling, {}, {}", nextDistancePrediction, EYE_SIGHT_DISTANCE);
        engine.setObjective(STOP_AND_ROLL);
        engine.roll();
        if (engine.getSpeed() > ROLLING_SPEED) {
          engine.fullBreak();
        }
      } else if (nextDistancePrediction < calculateBreakingDistance(FULL_TRAIN_DECELERATION)) {
        engine.setObjective(STOP_THEN_ROLL);
        logger.debug("{} stop then roll, {}, {}", nextDistancePrediction,
            calculateBreakingDistance(FULL_TRAIN_DECELERATION));
        engine.fullBreak();
      } else {

        if (!engine.isEmergencyBreaking() && engine.getObjective() != STOP_THEN_ROLL) {
          logger.debug(
              "{} FULL breaking, there's a train within breaking distance {} ({}) full breaking distance: {}",
              train, nextDistancePrediction, safeBreakingDistance,
              calculateBreakingDistance(FULL_TRAIN_DECELERATION));
          //engine.emergencyBreak();
          engine.fullBreak();
          //sendSquawkDownTheLine(RadioSignal.NOK);
          //engine.roll();
          engine.setObjective(STOP_THEN_ROLL);
        }
      }
    } else {
      if (engine.getObjective() == STOP_AND_ROLL) {
        if (nextDistancePrediction + arbitaryCloseDistance >= safeBreakingDistance) {
          return;
        }
        logger.warn("Keeping a distance [{}]", train);
        engine.setObjective(PROCEED);
      }

      if (engine.getObjective() == PROCEED_WITH_CAUTION && !seeingTrainsAhead) {
        logger.warn("It's probably safe to proceed [{}]", train);
        engine.setObjective(PROCEED);
      }
      if (engine.getObjective() == PROCEED && nextDistancePrediction >= 0 &&
          (engine.getTargetSpeed() <= engine.getLastAdvisorySpeed()
              || engine.getSpeed() > getMaxSpeed())) {
        engine.setTargetSpeed(Math.min(getMaxSpeed(), engine.getLastAdvisorySpeed()));
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

  public double calculateBreakingDistance() {
    double currentSpeed = engine.getSpeed();
    double targetSpeed = 0;
    double deceleration = engine.getMaxDeceleration();

    return DistanceHelper.distanceToReachTargetSpeed(targetSpeed, currentSpeed, deceleration);
  }

  public double calculateBreakingDistance(double decelerationSpeed) {
    double currentSpeed = engine.getSpeed();
    double targetSpeed = 0;
    double deceleration = decelerationSpeed;

    return DistanceHelper.distanceToReachTargetSpeed(targetSpeed, currentSpeed, deceleration);
  }

  private void calculateSafeBreakingDistance(double decelerationSpeed) {
    double distance = calculateBreakingDistance(decelerationSpeed);

    double safetyMargin = SAFETY_DISTANCE_MARGIN_COEFFICIENT * distance;

    this.safeBreakingDistance = distance + safetyMargin;
  }

  private void calculateSafeBreakingDistance() {
    calculateSafeBreakingDistance(engine.getMaxDeceleration());
  }

  private double getMaxSpeed() {
    double acceleration = engine.getNormalDeceleration();
    double distance = nextTrainPredictor.getDistance();
    double maxSpeed = Math.sqrt(-2.0 * acceleration * distance);

    return maxSpeed;
  }

  JourneyTimer getTimer() {
    return timer;
  }

  public boolean isTrainAheadBroken() {
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
    if (timeReceivedLastSignal + 2 * RealWorldConstants.TRAIN_SQUAWK_INTERVAL < timer.getTime()) {
      logger.warn("did not hear from next train, likely broken for {}", train);
      return true;
    }
    return false;
  }

  public JourneyPlan getJourneyPlan() {
    return journeyPlan;
  }

  void ping(RadioSignal radioSignal) {
    timeReceivedLastSignal = timer.getTime();
    this.lastRadioSignal = radioSignal;
    if (lastRadioSignal == RadioSignal.NOK) {
      StatsHelper.logFor(StatisticsVariable.GSM_GOT_NOK, train);
    }
  }

  public Optional<RadioMast> getRadioMast() {
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
