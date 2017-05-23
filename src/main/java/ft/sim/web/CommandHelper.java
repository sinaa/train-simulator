package ft.sim.web;

import static ft.sim.world.train.TrainObjective.PROCEED;
import static ft.sim.world.train.TrainObjective.STOP;

import ft.sim.simulation.SimulationController;
import java.util.Map;

/**
 * Created by sina on 25/04/2017.
 */
public class CommandHelper {

  static boolean processSetCommand(SimulationController simulation, Map<String, String> map) {
    String set = map.get("set");
    switch (set) {
      case "trainTargetSpeed": {
        int trainID = Integer.valueOf(map.get("targetID"));
        double targetSpeed = Double.valueOf(map.get("data"));
        simulation.getWorld().getTrain(trainID).getEngine()
            .setTargetSpeed(targetSpeed);
        if (targetSpeed > 0) {
          simulation.getWorld().getTrain(trainID).getEngine().setObjective(PROCEED);
        } else {
          simulation.getWorld().getTrain(trainID).getEngine().setObjective(STOP);
        }
        return true;
      }
      case "emergencyBrake": {
        int trainID = Integer.valueOf(map.get("targetID"));
        simulation.getWorld().getTrain(trainID).crash();
        return true;
      }
      case "worldMap": {
        String mapKey = map.get("data");
        if (simulation == null) {
          SimulationController.getInstance(mapKey);
        } else {
          simulation.setWorld(mapKey);
        }
        return true;
      }
    }
    return false;
  }

  static boolean processCommand(SimulationController simulation, Map<String, String> map,
      SocketSession socketSession) {
    String command = map.get("command");
    switch (command) {
      case "start trains":
        simulation.startTrains();
        return true;
      case "stop simulation":
        simulation.kill();
        return true;
      case "get push data":
        if (simulation != null) {
          simulation.setSocketSession(socketSession);
          return true;
        }
        return false;
      case "start simulation":
        simulation.startSimulation();
        simulation.setSocketSession(socketSession);
        return true;
      case "toggle interactive":
        simulation.toggleInteractive();
        return true;

    }
    return false;
  }
}
