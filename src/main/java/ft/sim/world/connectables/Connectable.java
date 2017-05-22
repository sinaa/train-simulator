package ft.sim.world.connectables;

import ft.sim.world.train.Train;

/**
 * Created by Sina on 21/02/2017.
 */
public interface Connectable {
  ConnectableType type = null;
  double getLength();

  public void entered(Train train);
  public void left(Train train);

  public int getID();
  public void setID(int id);
}
