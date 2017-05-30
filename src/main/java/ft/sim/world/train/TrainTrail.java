package ft.sim.world.train;

import ft.sim.world.connectables.Observable;
import ft.sim.world.connectables.Section;
import ft.sim.world.placeables.Placeable;

/**
 * Created by sina on 25/04/2017.
 */
public class TrainTrail implements Observable, Placeable {

  private transient Train train;
  private transient Section section;

  TrainTrail(Train train) {
    this.train = train;
  }

  public Train getTrain() {
    return train;
  }

  public void nowOnSection(Section newSection) {
    if (section != null && !section.equals(newSection)) {
      section.removePlacebale(this);
    }
    section = newSection;
  }

  public void atStation() {
    nowOnSection(null);
  }

  @Override
  public String toString() {
    return "Trail for: " + train.toString();
  }
}
