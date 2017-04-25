package ft.sim.web;

import static ft.sim.train.TrainObjective.PROCEED;
import static ft.sim.train.TrainObjective.STOP;

import ft.sim.simulation.BasicSimulation;
import ft.sim.train.TrainObjective;
import java.util.Map;

/**
 * Created by sina on 25/04/2017.
 */
public class CommandHelper {

  static boolean processSetCommand(BasicSimulation simulation, Map<String, String> map) {
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
      case "emergencyBreak": {
        int trainID = Integer.valueOf(map.get("targetID"));
        simulation.getWorld().getTrain(trainID).getEngine()
            .emergencyBreak();
        simulation.getWorld().getTrain(trainID).getEngine().setObjective(STOP);
        return true;
      }
      case "worldMap": {
        String mapKey = map.get("data");
        if (simulation == null) {
          BasicSimulation.getInstance(mapKey);
        } else {
          simulation.setWorld(mapKey);
        }
        return true;
      }
    }
    return false;
  }

  static boolean processCommand(BasicSimulation simulation, Map<String, String> map,
      SocketSession socketSession) {
    String command = map.get("command");
    switch (command) {
      case "start trains":
        simulation.startTrains();
        return true;
      case "stop simulation":
        simulation.removeSocketSessions(socketSession);
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
