package ft.sim.world.connectables;

import ft.sim.world.placeables.LCU;
import ft.sim.simulation.Tickable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Sina on 21/02/2017.
 */
public class Switch implements Connectable, Tickable {

  protected transient final Logger logger = LoggerFactory.getLogger(Switch.class);

  private final ConnectableType type = ConnectableType.SWITCH;

  private List<Track> from;
  private List<Track> to;

  private Map<Track, Track> status = new HashMap<Track, Track>(2);

  // delay of the switch to change position
  private static final double delay = 10;

  private double delayed = 0;
  private List<Track> newStatus = new ArrayList<>(2);

  private LCU lcu;

  // By default, a switch is 5 metres long
  private int length = 5;

  boolean isChanging = false;

  public Switch(List<Track> a, List<Track> b) {
    this(a,b, a.get(0), b.get(0));
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

  private void setSignals(){
    //TODO: set signals based on the status and connected tracks

  }

  public Map<Track, Track> getStatus() {
    return status;
  }

  @Override
  public double getLength() {
    return length;
  }
}
