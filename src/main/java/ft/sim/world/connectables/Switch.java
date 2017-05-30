package ft.sim.world.connectables;

import static ft.sim.world.signalling.SignalType.GREEN;
import static ft.sim.world.signalling.SignalType.RED;

import ft.sim.simulation.Tickable;
import ft.sim.world.WorldHandler;
import ft.sim.world.signalling.SignalController;
import ft.sim.world.signalling.SignalLinked;
import ft.sim.world.train.Train;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Sina on 21/02/2017.
 */
public class Switch implements Connectable, Tickable, SignalLinked {

  protected static transient final Logger logger = LoggerFactory.getLogger(Switch.class);
  // delay of the switch to change position
  private static final double delay = 10;
  private final ConnectableType type = ConnectableType.SWITCH;
  private boolean isChanging = false;
  private List<Track> from;
  private List<Track> to;
  private Map<Track, Track> status = new HashMap<Track, Track>(2);
  private double delayed = 0;
  private List<Track> newStatus = new ArrayList<>(2);
  private SignalController lcu;
  // By default, a switch is 5 metres long
  private int length = 5;
  private SignalController signalController;

  private int switchID = 0;

  public Switch(List<Track> a, List<Track> b) {
    this(a, b, a.get(0), b.get(0));
  }

  public Switch(List<Track> a, List<Track> b, Track left, Track right) {
    from = a;
    to = b;

    if (a.size() == 0 || b.size() == 0) {
      throw new IllegalArgumentException();
    }

    status.put(left, right);
    status.put(right, left);
    //TODO: set signals on the left/right tracks to green, the remaining tracks to red
    setSignals();
  }

  public void changePosition(Track a, Track b) {
    if (isChanging) {
      return;
    }
    isChanging = true;

    newStatus.clear();
    newStatus.add(a);
    newStatus.add(b);

    delayed += delay;
  }

  public void tick(double time) {
    if (!isChanging) {
      return;
    }
    delayed -= time;

    if (delayed <= 0) {
      status.clear();
      status.put(newStatus.get(0), newStatus.get(1));
      status.put(newStatus.get(1), newStatus.get(0));
      setSignals();
    }
  }

  private void setSignals() {
    //TODO: set signals based on the status and connected tracks

  }

  public Map<Track, Track> getStatus() {
    return status;
  }

  @Override
  public double getLength() {
    return length;
  }

  @Override
  public void entered(Train train) {
    logger.info("{} signal controller: {}", this, RED);
    if (signalController != null) {
      signalController.setStatus(RED);
    }
  }

  @Override
  public void left(Train train) {
    logger.info("{} signal controller: {}", this, GREEN);
    if (signalController != null) {
      signalController.setStatus(GREEN);
    }
  }

  @Override
  public int getID() {
    return this.switchID;
  }

  @Override
  public void setID(int switchID) {
    if (this.switchID != 0) {
      throw new IllegalStateException("The ID can only be set once!");
    }
    this.switchID = switchID;
  }

  @Override
  public String toString() {
    try {
      return "Switch-" + WorldHandler.getInstance().getWorld().getSwitchID(this);
    } catch (Exception e) {
      return super.toString();
    }
  }

  @Override
  public void addSignalController(SignalController signalController) {
    this.signalController = signalController;
  }
}
