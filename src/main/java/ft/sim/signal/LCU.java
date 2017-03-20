package ft.sim.signal;

/**
 * Created by Sina on 20/03/2017.
 */
public class LCU {

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
