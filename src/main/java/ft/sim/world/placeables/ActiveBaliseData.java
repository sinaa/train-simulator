package ft.sim.world.placeables;

/**
 * Created by sina on 19/04/2017.
 */
public class ActiveBaliseData implements Cloneable{

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
}
