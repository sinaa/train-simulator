package ft.sim.signalling;

/**
 * Created by Sina on 03/04/2017.
 */
public interface SignalListener {

  void signalChange(SignalType type);

  void startListeningTo(SignalUnit signalUnit);

  void stopListeningTo(SignalUnit signalUnit);

}
