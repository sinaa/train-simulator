package ft.sim.signalling;

import ft.sim.world.connectables.Observable;
import ft.sim.world.placeables.Placeable;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Sina on 20/03/2017.
 */
public class SignalUnit implements Placeable, Observable {

  protected static Logger logger = LoggerFactory.getLogger(SignalUnit.class);

  private SignalType status = SignalType.GREEN;

  private Set<SignalListener> signalListeners = new HashSet<>();

  private final boolean distantSignal;

  public SignalUnit() {
    this(false);
  }

  public SignalUnit(boolean isDistantSignal) {
    distantSignal = isDistantSignal;
  }

  public boolean isDistantSignal() {
    return distantSignal;
  }

  public void setStatus(SignalType newStatus) {
    status = newStatus;
    signalListeners.forEach(l -> {
      l.signalChange(newStatus);
      l.stopListeningTo(this);
      logger.info("dist({}) notifying {} of {} signal", distantSignal, l, newStatus);
    });

    //FIXME: should this be cleared?
    signalListeners.clear();
  }

  public void addListener(SignalListener signalListener) {
    signalListeners.add(signalListener);
    signalListener.startListeningTo(this);
  }

  public void stopListening(SignalListener signalListener) {
    signalListeners.remove(signalListener);
    signalListener.stopListeningTo(this);
  }

  public SignalType getStatus() {
    return status;
  }
}
