package ft.sim.world.placeables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sina on 19/04/2017.
 */
public class ActiveBaliseData implements Cloneable{

  protected transient static final Logger logger = LoggerFactory.getLogger(ActiveBaliseData.class);

  private int lastTrainID = -1;

  private double timeLastTrainPassed = -1;

  private double trainSpeed = -1;

  private boolean isDecelerating = false;

  public ActiveBaliseData(){

  }

  public ActiveBaliseData(int lastTrainID, double timeLastTrainPassed, double trainSpeed, boolean isDecelerating) {
    setData(lastTrainID, timeLastTrainPassed, trainSpeed, isDecelerating);

  }

  public void setData(int lastTrainID, double timeLastTrainPassed, double trainSpeed,
      boolean isDecelerating) {
    this.lastTrainID = lastTrainID;
    this.timeLastTrainPassed = timeLastTrainPassed;
    this.trainSpeed = trainSpeed;
    this.isDecelerating = isDecelerating;
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
    return new ActiveBaliseData(lastTrainID, timeLastTrainPassed, trainSpeed, isDecelerating);
  }

  @Override
  public String toString() {
    return String.format("[FerrmoneBalise] LastTime: %s, Speed: %s, isDecelrating: %s", timeLastTrainPassed, trainSpeed, isDecelerating);
  }
}
