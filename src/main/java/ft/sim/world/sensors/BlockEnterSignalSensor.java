package ft.sim.world.sensors;

import static ft.sim.signalling.SignalType.RED;

import ft.sim.signalling.SignalController;
import ft.sim.train.Train;
import ft.sim.world.placeables.Placeable;

/**
 * Created by sina on 10/04/2017.
 */
public class BlockEnterSignalSensor implements Placeable, Sensor {

  SignalController signalController;

  public BlockEnterSignalSensor(SignalController signalController) {
    this.signalController = signalController;
  }

  @Override
  public void trigger(Train train) {
    signalController.setStatus(RED);
  }
}
