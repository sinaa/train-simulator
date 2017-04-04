package ft.sim.signalling;

import ft.sim.world.placeables.Placeable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Sina on 20/03/2017.
 */
public class SignalUnit implements Placeable{

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
    signalListeners.forEach(l -> l.signalChange(newStatus));

    signalListeners.clear();
  }

  public void addListener(SignalListener signalListener) {
    signalListeners.add(signalListener);
  }

  public void stopListening(SignalListener signalListener) {
    signalListeners.remove(signalListener);
  }

  public SignalType getStatus() {
    return status;
  }
}
