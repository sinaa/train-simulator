package ft.sim.world.connectables;

import ft.sim.world.placeables.Placeable;
import ft.sim.world.train.TrainTrail;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Sina on 21/02/2017.
 */
public class Section {

  // By default, each section is 1 metres
  private static final transient int DEFAULT_LENGTH = 1;
  private static final transient int length = Section.DEFAULT_LENGTH;
  Set<Placeable> placeables = new HashSet<>();


  public int getLength() {
    return length;
  }

  public void addPlaceable(Placeable p) {
    placeables.add(p);
    if (p instanceof TrainTrail) {
      ((TrainTrail) p).nowOnSection(this);
    }
  }

  public List<Placeable> getPlaceables() {
    return new ArrayList<>(placeables);
  }

  public void removePlacebale(Placeable placeable) {
    placeables.remove(placeable);
  }

}
