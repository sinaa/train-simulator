package ft.sim.signalling;

import ft.sim.world.connectables.Connectable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Sina on 20/03/2017.
 */
public class SignalController {

  protected static Logger logger = LoggerFactory.getLogger(SignalController.class);

  private SignalType status = SignalType.GREEN;

  private Set<SignalUnit> signalSet = new HashSet<>();
  private Set<SignalListener> signalListeners = new HashSet<>();

  transient Connectable belongsTo;

  public SignalController(Connectable belongsTo){
    this.belongsTo = belongsTo;
  }

  public void setStatus(SignalType status) {
    this.status = status;
    logger.info("{} signal controller set to {}", belongsTo, status);
    signalSet.forEach(signal -> signal.setStatus(status));
    //signalListeners.forEach(listener -> listener.signalChange(status, this));
  }

  public SignalType getStatus() {
    return status;
  }

  @Deprecated
  private void addListener(SignalListener signalListener) {
    signalListeners.add(signalListener);
  }

  @Deprecated
  private void removeSignalListener(SignalListener signalListener) {
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
    addSignalUnit(newDistantSignal);

    return newDistantSignal;
  }

  private SignalUnit newMainSignal() {
    SignalUnit newMainSignal = new SignalUnit(false);
    addSignalUnit(newMainSignal);

    return newMainSignal;
  }

  public SignalUnit getMainSignal() {
    return signalSet.stream().filter(s -> !s.isDistantSignal())
        .map(Optional::ofNullable).findFirst().flatMap(Function.identity()).orElse(newMainSignal());
  }

}
