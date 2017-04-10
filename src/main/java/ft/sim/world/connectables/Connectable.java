package ft.sim.world.connectables;

import ft.sim.train.Train;

/**
 * Created by Sina on 21/02/2017.
 */
public interface Connectable {
  ConnectableType type = null;
  double getLength();

  public void entered(Train train);
  public void left(Train train);
}
