package ft.sim.world.placeables;

/**
 * Created by Sina on 06/03/2017.
 */
public class PassiveBalise extends Balise {

  private double advisorySpeed = 0;

  private int baliseID = 0;

  private boolean isBroken = false;

  public PassiveBalise(double advisorySpeed, int baliseID) {
    this.advisorySpeed = advisorySpeed;
    this.baliseID = baliseID;
  }

  public double getAdvisorySpeed() {
    return advisorySpeed;
  }

  public void setAdvisorySpeed(int advisorySpeed) {
    this.advisorySpeed = advisorySpeed;
  }

  public int getBaliseID() {
    return baliseID;
  }

  public void setBaliseID(int baliseID) {
    this.baliseID = baliseID;
  }

  @Override
  public boolean isBroken() {
    return false;
  }

  @Override
  public void setIsBroken(boolean isBroken) {
    this.isBroken = isBroken;
  }
}
