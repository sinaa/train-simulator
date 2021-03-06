package ft.sim.world.connectables;

import ft.sim.statistics.Recordable;
import ft.sim.world.train.Train;

/**
 * Created by Sina on 21/02/2017.
 */
public interface Connectable extends Recordable {

  ConnectableType type = null;

  double getLength();

  void entered(Train train);

  void left(Train train);

  //public int getID();
  void setID(int id);
}
