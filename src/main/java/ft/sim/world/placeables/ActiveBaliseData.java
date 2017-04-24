package ft.sim.world.placeables;

import ft.sim.world.map.MapBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sina on 19/04/2017.
 */
public class ActiveBaliseData implements Cloneable{

  protected transient static final Logger logger = LoggerFactory.getLogger(ActiveBaliseData.class);

  private double timeLastTrainPassed = -1;

  private double trainSpeed = -1;

  private boolean isDecelerating = false;

  public ActiveBaliseData(){

  }

  public ActiveBaliseData(double timeLastTrainPassed, double trainSpeed, boolean isDecelerating) {
    setData(timeLastTrainPassed, trainSpeed, isDecelerating);

  }

  public void setData(double timeLastTrainPassed, double trainSpeed, boolean isDecelerating) {
    this.timeLastTrainPassed = timeLastTrainPassed;
    this.trainSpeed = trainSpeed;
    this.isDecelerating = isDecelerating;
    logger.info(this.toString());
  }

  public double getTimeLastTrainPassed() {
    return timeLastTrainPassed;
  }

  public double getTrainSpeed() {
    return trainSpeed;
  }

  public boolean isDecelerating() {
    return isDecelerating;
  }

  @Override
  public ActiveBaliseData clone() {
    return new ActiveBaliseData(timeLastTrainPassed, trainSpeed, isDecelerating);
  }

  @Override
  public String toString() {
    return String.format("[FerrmoneBalise] LastTime: %s, Speed: %s, isDecelrating: %s", timeLastTrainPassed, trainSpeed, isDecelerating);
  }
}
