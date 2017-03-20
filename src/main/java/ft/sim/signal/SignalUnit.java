package ft.sim.signal;

/**
 * Created by Sina on 20/03/2017.
 */
public class SignalUnit {

  SignalType status = SignalType.GREEN;

  public SignalUnit(){

  }

  public void setStatus(SignalType newStatus){
    status = newStatus;
  }

}
