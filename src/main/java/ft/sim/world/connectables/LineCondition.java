package ft.sim.world.connectables;

import ft.sim.world.RealWorldConstants;

/**
 * Created by sina on 15/05/2017.
 */
public class LineCondition implements Cloneable {


  private double accelerationCoefficient = RealWorldConstants.ACCELERATION_COEFFICIENT;
  private double decelerationCoefficient = RealWorldConstants.DECELERATION_COEFFICIENT;


  public LineCondition() {
    // use defaults
  }

  public LineCondition(double accelerationCoefficient, double decelerationCoefficient) {
    this.accelerationCoefficient = accelerationCoefficient;
    this.decelerationCoefficient = decelerationCoefficient;
  }

  public double getAccelerationCoefficient() {
    return accelerationCoefficient;
  }

  public void setAccelerationCoefficient(double accelerationCoefficient) {
    this.accelerationCoefficient = accelerationCoefficient;
  }

  public double getDecelerationCoefficient() {
    return decelerationCoefficient;
  }

  public void setDecelerationCoefficient(double decelerationCoefficient) {
    this.decelerationCoefficient = decelerationCoefficient;
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    return new LineCondition(accelerationCoefficient, decelerationCoefficient);
  }
}
