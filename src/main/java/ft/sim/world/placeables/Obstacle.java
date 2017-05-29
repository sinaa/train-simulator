package ft.sim.world.placeables;

/**
 * Created by sina on 22/05/2017.
 */
public class Obstacle implements Placeable {

  private boolean isHit = false;

  /**
   * The obstacle can be hit only once.
   * @return true if it hasn't been hit before, and false for any further hits.
   */
  public boolean hit() {
    if (isHit) {
      return false;
    }
    isHit = true;
    return true;
  }

}
