package ft.sim.world;

/**
 * Created by Sina on 06/03/2017.
 */
public class FixedBalise implements Balise {

  private double advisorySpeed = 0;

  private int baliseID = 0;

  public FixedBalise(double advisorySpeed, int baliseID) {
    this.advisorySpeed = advisorySpeed;
    this.baliseID = baliseID;
  }

  public void setAdvisorySpeed(int advisorySpeed) {
    this.advisorySpeed = advisorySpeed;
  }

  public double getAdvisorySpeed() {
    return advisorySpeed;
  }

  public void setBaliseID(int baliseID) {
    this.baliseID = baliseID;
  }

  public int getBaliseID() {
    return baliseID;
  }

}
