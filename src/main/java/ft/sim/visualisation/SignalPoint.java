package ft.sim.visualisation;

import ft.sim.world.signalling.SignalType;

/**
 * Created by sina on 17/05/2017.
 */
public class SignalPoint {

  int offset = 0;
  int trackID = 0;
  String status = "green";

  public SignalPoint(int offset, int trackID, SignalType status) {
    this.offset = offset;
    this.trackID = trackID;
    switch (status) {
      case RED:
        this.status = "red";
        break;
      case GREEN:
        this.status = "green";
        break;
    }
  }
}
