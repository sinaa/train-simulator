package ft.sim.world.placeables;

import ft.sim.simulation.Disruptable;

/**
 * Created by Sina on 06/03/2017.
 */
public class ActiveBalise extends Balise implements Disruptable {

  private ActiveBaliseData data = new ActiveBaliseData();

  private boolean isBroken = false;

  public void update(double timePassing, double speed, boolean isDecelerating){
    data.setData(timePassing, speed, isDecelerating);
  }

  public ActiveBaliseData getData() {
    return data.clone();
  }

  @Override
  public boolean isBroken() {
    return isBroken;
  }

  @Override
  public void setIsBroken(boolean isBroken) {
    this.isBroken = isBroken;
  }
}
