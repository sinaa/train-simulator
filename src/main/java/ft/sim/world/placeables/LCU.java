package ft.sim.world.placeables;

import ft.sim.signal.SignalType;
import ft.sim.signal.SignalUnit;

/**
 * Created by Sina on 20/03/2017.
 */
public class LCU implements Placeable{

  SignalUnit distantSignalUnit;
  SignalUnit signal;

  public LCU(SignalUnit distantSignal){
    distantSignalUnit = distantSignal;
    signal = new SignalUnit();
  }

  public void setStatus(SignalType status){
    distantSignalUnit.setStatus(status);
    signal.setStatus(status);
  }

}
