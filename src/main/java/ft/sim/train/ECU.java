package ft.sim.train;

import static ft.sim.train.TrainObjective.*;

import ft.sim.physics.DistanceHelper;
import ft.sim.simulation.Tickable;
import ft.sim.world.journey.Journey;
import ft.sim.world.journey.JourneyPath;
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
  private transient JourneyPath journeyPath;
  private transient Train train;
  private JourneyTimer timer;
  private NextTrainPrediction nextTrainPrediction = new NextTrainPrediction();

  // safe breaking distance, in meters
  private double safeBreakingDistance;

  private double totalDistanceTravelledLastBalise = 0;

  public ECU(Journey journey, Engine engine) {
    this.engine = engine;
    createWorldModel(journey);
  }

  public void updateNextTrainPrediction(ActiveBaliseData lastBaliseData) {
    nextTrainPrediction.setLastData(lastBaliseData);
    totalDistanceTravelledLastBalise = engine.getTotalDistanceTravelled();
    //TODO: update prediction ?
  }

  private void createWorldModel(Journey journey) {
    this.journeyPath = journey.getJourneyPath();
    this.train = journey.getTrain();

    this.safeBreakingDistance = journeyPath.getLength();
    this.timer = journey.getJourneyTimer();
  }

  public void tick(double time) {
    calculateSafeBreakingDistance();
    nextTrainPrediction.predict(timer.getTime(),
        engine.getTotalDistanceTravelled() - totalDistanceTravelledLastBalise);
    if (nextTrainPrediction.getDistance() < safeBreakingDistance) {
      engine.emergencyBreak();
      engine.roll();
      engine.setObjective(STOP_AND_ROLL);
    } else {
      if (engine.getObjective() == STOP_AND_ROLL) {
        engine.setObjective(PROCEED);
      }
    }

  }

  public double calculateBreakingDistance(){
    double currentSpeed = engine.getSpeed();
    double targetSpeed = 0;
    double deceleration = engine.getMaxDeceleration();

    double distance = DistanceHelper
        .distanceToReachTargetSpeed(targetSpeed, currentSpeed, deceleration);
    return distance;
  }

  private void calculateSafeBreakingDistance() {
    double distance = calculateBreakingDistance();

    double safetyMargin = SAFETY_DISTANCE_MARGIN_COEFFICIENT * distance;

    this.safeBreakingDistance = distance + safetyMargin;
  }

  public JourneyTimer getTimer() {
    return timer;
  }
}
