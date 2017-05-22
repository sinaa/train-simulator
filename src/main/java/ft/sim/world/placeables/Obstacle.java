package ft.sim.world.placeables;

/**
 * Created by sina on 22/05/2017.
 */
public class Obstacle implements Placeable {

  private boolean isHit = false;

  public boolean hit() {
    if (isHit) {
      return false;
    }
    isHit = true;
    return true;
  }

}
