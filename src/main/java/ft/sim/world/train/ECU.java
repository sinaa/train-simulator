package ft.sim.world.train;

import static ft.sim.world.train.TrainObjective.PROCEED;
import static ft.sim.world.train.TrainObjective.PROCEED_WITH_CAUTION;
import static ft.sim.world.train.TrainObjective.STOP_AND_ROLL;
import static ft.sim.world.train.TrainObjective.STOP_THEN_ROLL;

import ft.sim.physics.DistanceHelper;
import ft.sim.simulation.Tickable;
import ft.sim.world.RealWorldConstants;
import ft.sim.world.gsm.RadioMast;
import ft.sim.world.gsm.RadioSignal;
import ft.sim.world.journey.Journey;
import ft.sim.world.journey.JourneyPlan;
import ft.sim.world.journey.JourneyTimer;
import ft.sim.world.placeables.ActiveBaliseData;
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

  public ECU(Journey journey, Engine engine) {
    this.engine = engine;
    createWorldModel(journey);
  }

  public void setSeeingTrainsAhead(boolean seeingTrainsAhead) {
    this.seeingTrainsAhead = seeingTrainsAhead;
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
    calculateSafeBreakingDistance();
    if (!nextTrainPredictor.anyTrainsAhead()) {
      return;
    }

    if (isTrainAheadBroken()) {
      engine.emergencyBreak();
      engine.setObjective(STOP_THEN_ROLL);
      return;
    }

    nextTrainPredictor.predict(timer.getTime(), getEstimatedDistanceTravelledSinceLastBalise());
    double nextDistancePrediction = nextTrainPredictor.getDistance();
    /*if (nextDistancePrediction != -1) {
      logger.info("[{}] Next train is {} meters away. safe distance: {}", train,
          nextDistancePrediction, safeBreakingDistance);
    }*/
    if (engine.getSpeed() > 1 && nextDistancePrediction >= 0
        && nextDistancePrediction < safeBreakingDistance) {
      if (nextDistancePrediction > calculateBreakingDistance(engine.getNormalDeceleration())) {
        engine.setTargetSpeed(0);
        engine.setObjective(STOP_THEN_ROLL);
        logger.warn("Train {} normal breaking, there's a train within breaking distance {} ({}) {}",
            train, nextDistancePrediction, safeBreakingDistance,
            calculateBreakingDistance(engine.getNormalDeceleration()));
      } else {

        logger.warn("Train {} emergency breaking, there's a train within breaking distance {} ({})",
            train, nextDistancePrediction, safeBreakingDistance);
        engine.emergencyBreak();
        //engine.roll();
        engine.setObjective(STOP_THEN_ROLL);
      }
      return;
    } else {
      if ((engine.getObjective() == STOP_AND_ROLL || engine.getObjective() == PROCEED_WITH_CAUTION)
          && !seeingTrainsAhead) {
        logger.warn("It's probably safe to proceed [{}]", train);
        engine.setObjective(PROCEED);
      }
      if (engine.getObjective() == PROCEED && nextDistancePrediction >= 0 &&
          engine.getTargetSpeed() <= engine.getLastAdvisorySpeed()) {
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
    //TODO: add inaccuracies
    double realTravelled = engine.getTotalDistanceTravelled() - totalDistanceTravelledLastBalise;
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
    double deceleration = engine.getMaxDeceleration();

    return DistanceHelper.distanceToReachTargetSpeed(targetSpeed, currentSpeed, deceleration);
  }

  private void calculateSafeBreakingDistance() {
    double distance = calculateBreakingDistance();

    double safetyMargin = SAFETY_DISTANCE_MARGIN_COEFFICIENT * distance;

    this.safeBreakingDistance = distance + safetyMargin;
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
    return lastRadioSignal == RadioSignal.NOK
        || timeReceivedLastSignal + 2 * RealWorldConstants.TRAIN_SQUAWK_INTERVAL < timer.getTime();
  }

  public JourneyPlan getJourneyPlan() {
    return journeyPlan;
  }

  void ping(RadioSignal radioSignal) {
    timeReceivedLastSignal = timer.getTime();
    this.lastRadioSignal = radioSignal;
  }

  public RadioMast getRadioMast() {
    return radioMast;
  }

  public void setRadioMast(RadioMast radioMast) {
    this.radioMast = radioMast;
  }
}
