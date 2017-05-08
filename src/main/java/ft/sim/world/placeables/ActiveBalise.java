package ft.sim.world.placeables;

import ft.sim.simulation.Disruptable;

/**
 * Created by Sina on 06/03/2017.
 */
public class ActiveBalise extends Balise implements Disruptable {

  private ActiveBaliseData data = new ActiveBaliseData();

  // data brought over by a train on the opposite side
  private ActiveBaliseData upAheadData = new ActiveBaliseData();

  private transient ActiveBalise dualTrackPair = null;

  private boolean isBroken = false;

  public void update(int lastTrainID, double timePassing, double speed, boolean isDecelerating) {
    data.setData(lastTrainID, timePassing, speed, isDecelerating);
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

  public void setDualTrackPair(ActiveBalise dualTrackPair) {
    if (this.dualTrackPair != null) {
      throw new IllegalStateException("Dual pair has already been set for this balise: " + this);
    }
    this.dualTrackPair = dualTrackPair;
  }

  public ActiveBaliseData getOtherSideData() {
    return (dualTrackPair != null) ? dualTrackPair.getData() : null;
  }

  public void setUpAheadData(ActiveBaliseData broughtForwardData) {
    this.upAheadData = broughtForwardData;
  }

  public ActiveBaliseData getUpAheadData() {
    return upAheadData.clone();
  }
}
