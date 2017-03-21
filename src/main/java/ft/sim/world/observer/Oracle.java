package ft.sim.world.observer;

import ft.sim.world.map.GlobalMap;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sina on 21/03/2017.
 */
public enum Oracle {

  instance;

  private List<Violation> violations = new ArrayList<>();

  public void addViolation(Violation violation) {
    violations.add(violation);
  }

  public void checkState(GlobalMap world, long tick){

  }

}
