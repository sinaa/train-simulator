package ft.sim.signalling;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

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

  public void syncSignals() {
    SignalUnit mainSignal = getMainSignal();
    if (mainSignal == null) {
      return;
    }
    SignalType status = mainSignal.getStatus();
    signalSet.stream().filter(SignalUnit::isDistantSignal).forEach(s -> s.setStatus(status));
  }

  public SignalUnit newDistantSignal() {
    SignalUnit mainSignal = getMainSignal();
    if (mainSignal == null) {
      return null;
    }
    SignalUnit newDistantSignal = new SignalUnit(true);
    newDistantSignal.setStatus(mainSignal.getStatus());
    signalSet.add(newDistantSignal);

    return newDistantSignal;
  }

  private SignalUnit newMainSignal() {
    SignalUnit newMainSignal = new SignalUnit(false);
    signalSet.add(newMainSignal);

    return newMainSignal;
  }

  public SignalUnit getMainSignal() {
    return signalSet.stream().filter(s -> !s.isDistantSignal())
        .map(Optional::ofNullable).findFirst().flatMap(Function.identity()).orElse(null);
  }

}
