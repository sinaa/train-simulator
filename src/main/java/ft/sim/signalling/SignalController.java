package ft.sim.signalling;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Sina on 20/03/2017.
 */
public class SignalController {

  Set<SignalUnit> signalSet = new HashSet<>();
  Set<SignalListener> signalListeners = new HashSet<>();


  public void setStatus(SignalType status) {
    signalSet.forEach(signal -> signal.setStatus(status));
    signalListeners.forEach(listener -> listener.signalChange(status));
  }

  public void addListener(SignalListener signalListener) {
    signalListeners.add(signalListener);
  }

  public void removeSignalListener(SignalListener signalListener) {
    signalListeners.remove(signalListener);
  }

  public void addSignalUnit(SignalUnit signalUnit) {
    signalSet.add(signalUnit);
  }

}
