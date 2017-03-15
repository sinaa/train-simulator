package ft.sim.world.connectables;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Sina on 21/02/2017.
 */
public class Switch implements Connectable {

  protected transient final Logger logger = LoggerFactory.getLogger(Switch.class);

  private final ConnectableType type = ConnectableType.SWITCH;

  private List<Track> from;
  private List<Track> to;

  private Map<Track, Track> status = new HashMap<Track, Track>(2);

  // By default, a switch is 5 metres long
  private int length = 5;

  public Switch(List<Track> a, List<Track> b) {
    from = a;
    to = b;

    if (a.size() == 0 || b.size() == 0) {
      throw new IllegalArgumentException();
    }

    setStatus(a.get(0), b.get(0));
  }

  public void setStatus(Track a, Track b) {
    status.clear();
    status.put(a, b);
    status.put(b, a);
  }

  public Map<Track, Track> getStatus() {
    return status;
  }

  @Override
  public double getLength() {
    return length;
  }
}
