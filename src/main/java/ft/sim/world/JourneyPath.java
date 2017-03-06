package ft.sim.world;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import ft.sim.world.Connectable;
import ft.sim.world.Track;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Sina on 21/02/2017.
 */
public class JourneyPath {

  protected Logger logger = LoggerFactory.getLogger(JourneyPath.class);

  private List<Connectable> path = new ArrayList<>();
  private Map<Connectable, Double> connectablePositions = new HashMap<>();
  private BiMap<Connectable, Integer> connectableIndexes = HashBiMap.create();

  private double length = 0;

  public JourneyPath(List<Connectable> path) {
    this.path.addAll(path);
    init();
  }

  public double getConnectableStartingPosition(Connectable connectable) {
    if(connectable == null)
      throw new NullPointerException("cannot get position of null connectable!!");
    return connectablePositions.get(connectable);
  }

  private void init() {
    // already initialised
    if(length != 0)
      return;

    for (Connectable c : path) {
      connectablePositions.put(c, length);
      connectableIndexes.put(c, connectableIndexes.size());
      length += c.getLength();
      //logger.debug("Connectable length: {}", c.getLength());
    }
  }

  public double getLength() {
    return length;
  }

  public Connectable getNext(Connectable c) {
    int index = connectableIndexes.get(c);
    int nextIndex = index + 1;
    if(nextIndex >= connectableIndexes.size())
      return null;
    return path.get(nextIndex);
  }

  public List<Connectable> getPath() {
    return path;
  }

  public List<Connectable> getConnectablesBetween(double from, double to) {

    List<Connectable> connectables = new ArrayList<>();

    if (from < 0 || to > getLength()) {
      throw new IllegalArgumentException("Invalid From/To arguments");
    }

    double calculated = 0;
    for (Connectable c : path) {
      double connectableLength = c.getLength();

      if (connectables.isEmpty()) {
        if (from >= calculated && from < calculated + connectableLength) {
          connectables.add(c);
        }
      } else {
        if (to >= calculated) {
          connectables.add(c);
        } else {
          break;
        }
      }

      calculated += c.getLength();
    }

    return connectables;
  }

}
