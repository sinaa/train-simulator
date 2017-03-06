package ft.sim.world;

/**
 * Created by Sina on 06/03/2017.
 */
public class FixedBalise implements Balise {

  private int advisorySpeed = 0;

  public FixedBalise(int advisorySpeed) {
    this.advisorySpeed = advisorySpeed;
  }

  public void setAdvisorySpeed(int advisorySpeed) {
    this.advisorySpeed = advisorySpeed;
  }

  public int getAdvisorySpeed() {
    return advisorySpeed;
  }
}
