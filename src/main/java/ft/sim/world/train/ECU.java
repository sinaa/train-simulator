package ft.sim.world.train;

import static ft.sim.world.train.TrainObjective.*;

import ft.sim.physics.DistanceHelper;
import ft.sim.simulation.Tickable;
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
  private NextTrainPrediction nextTrainPrediction = new NextTrainPrediction();
  private boolean seeingTrainsAhead = false;

  private RadioMast radioMast;
  private RadioSignal lastRadioSignal = null;
  private double timeReceivedLastSignal = -1;


  public void setSeeingTrainsAhead(boolean seeingTrainsAhead) {
    this.seeingTrainsAhead = seeingTrainsAhead;
  }

  // safe breaking distance, in meters
  private double safeBreakingDistance;

  private double totalDistanceTravelledLastBalise = 0;
  private double totalDistanceLastUpAhead = 0;

  public ECU(Journey journey, Engine engine) {
    this.engine = engine;
    createWorldModel(journey);
  }

  public void updateNextTrainPrediction(ActiveBaliseData lastBaliseData) {
    nextTrainPrediction.setLastData(lastBaliseData);
    totalDistanceTravelledLastBalise = engine.getTotalDistanceTravelled();
    //TODO: update prediction ?
  }

  public void updateUpAheadData(ActiveBaliseData upAhead){
    double howFarAhead = engine.getTotalDistanceTravelled() - totalDistanceLastUpAhead;
    nextTrainPrediction.setUpAheadData(upAhead, howFarAhead);
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
    if (!nextTrainPrediction.anyTrainsAhead()) {
      return;
    }

    nextTrainPrediction.predict(timer.getTime(), getEstimatedDistanceTravelledSinceLastBalise());
    double nextDistancePrediction = nextTrainPrediction.getDistance();
    /*if (nextDistancePrediction != -1) {
      logger.info("[{}] Next train is {} meters away. safe distance: {}", train,
          nextDistancePrediction, safeBreakingDistance);
    }*/
    if (engine.getSpeed() > 1 && nextDistancePrediction >= 0
        && nextDistancePrediction < safeBreakingDistance) {
      logger.warn("Train {} emergency breaking, there's a train within breaking distance {} ({})",
          train, nextDistancePrediction, safeBreakingDistance);
      engine.emergencyBreak();
      //engine.roll();
      engine.setObjective(STOP_THEN_ROLL);
    } else {
      if ((engine.getObjective() == STOP_AND_ROLL || engine.getObjective() == PROCEED_WITH_CAUTION)
          && !seeingTrainsAhead) {
        logger.warn("It's probably safe to proceed [{}]", train);
        engine.setObjective(PROCEED);
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

  private void calculateSafeBreakingDistance() {
    double distance = calculateBreakingDistance();

    double safetyMargin = SAFETY_DISTANCE_MARGIN_COEFFICIENT * distance;

    this.safeBreakingDistance = distance + safetyMargin;
  }

  JourneyTimer getTimer() {
    return timer;
  }

  public JourneyPlan getJourneyPlan() {
    return journeyPlan;
  }

  void ping(RadioSignal radioSignal){
    timeReceivedLastSignal = timer.getTime();
    this.lastRadioSignal = radioSignal;
  }

  public void setRadioMast(RadioMast radioMast) {
    this.radioMast = radioMast;
  }

  public RadioMast getRadioMast() {
    return radioMast;
  }
}
