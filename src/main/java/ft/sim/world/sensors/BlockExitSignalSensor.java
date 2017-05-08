package ft.sim.world.sensors;

import static ft.sim.world.signalling.SignalType.GREEN;

import ft.sim.world.signalling.SignalController;
import ft.sim.world.train.Train;
import ft.sim.world.placeables.Placeable;

/**
 * Created by sina on 10/04/2017.
 */
public class BlockExitSignalSensor implements Placeable, Sensor {

  SignalController signalController;

  public BlockExitSignalSensor(SignalController signalController) {
    this.signalController = signalController;
  }

  @Override
  public void trigger(Train train) {
    signalController.setStatus(GREEN);
  }
}
