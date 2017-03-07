package ft.sim.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Sina on 21/02/2017.
 */
public class Section {

  private static final transient int length = Section.DEFAULT_LENGTH;

  // By default, each section is 1 metres
  public static final transient int DEFAULT_LENGTH = 1;

  Set<Placeable> placeables = new HashSet<>();


  public int getLength(){
    return length;
  }

  public void addPlaceable(Placeable p){
    placeables.add(p);
  }

  public List<Placeable> getPlaceables() {
    return new ArrayList<>(placeables);
  }
}
