package ft.sim.world.placeables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Sina on 06/03/2017.
 */
public class ActiveBalise extends Balise {

  protected transient static final Logger logger = LoggerFactory.getLogger(ActiveBalise.class);

  private ActiveBaliseData data = new ActiveBaliseData();

  // data brought over by a train on the opposite side
  private ActiveBaliseData upAheadData = new ActiveBaliseData();

  private transient ActiveBalise dualTrackPair = null;

  private boolean isBroken = false;

  public void update(int lastTrainID, double timePassing, double speed, boolean isDecelerating) {
    if (!isBroken) {
      data.setData(lastTrainID, timePassing, speed, isDecelerating);
    }
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
    logger.info("Balise {} is set to broken!");
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

  public ActiveBaliseData getUpAheadData() {
    return upAheadData.clone();
  }

  public void setUpAheadData(ActiveBaliseData broughtForwardData) {
    if (!isBroken) {
      this.upAheadData = broughtForwardData;
    }
  }
}
