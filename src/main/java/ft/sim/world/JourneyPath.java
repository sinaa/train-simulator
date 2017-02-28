package ft.sim.world;

import ft.sim.world.Connectable;
import ft.sim.world.Track;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Sina on 21/02/2017.
 */
public class JourneyPath {

  private List<Connectable> path = new ArrayList<>();

  private int length = 0;

  public JourneyPath(List<Connectable> path) {
    this.path.addAll(path);
  }

  public int getLength() {
    if (length == 0) {
      for (Connectable c : path) {
        length += c.getLength();
      }
    }
    return length;
  }

}
