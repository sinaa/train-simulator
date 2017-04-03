package ft.sim.signalling;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Sina on 20/03/2017.
 */
public class SignalUnit {

  SignalType status = SignalType.GREEN;

  Set<SignalListener> signalListeners = new HashSet<>();

  public SignalUnit(){

  }

  public void setStatus(SignalType newStatus){
    status = newStatus;
    signalListeners.forEach(l->l.signalChange(newStatus));
    signalListeners.clear();
  }

  public void addListener(SignalListener signalListener) {
    signalListeners.add(signalListener);
  }



}
