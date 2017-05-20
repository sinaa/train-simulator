package ft.sim.experiment;

import ft.sim.simulation.SimulationController;
import ft.sim.world.map.MapBuilder;

/**
 * Created by sina on 20/05/2017.
 */
public class Experiment {

  SimulationController simulation;
  ExperimentListenerInterface experimentListener = null;

  public Experiment(){
    simulation = SimulationController.getInstance(SimulationController.DEFAULT_MAP);
    simulation.setNonInteractve();
    simulation.setExperiment(this);
  }

  void run(ExperimentListenerInterface experimentController) {
    simulation.startSimulation();
    this.experimentListener = experimentController;
  }

  public void finished(){
    experimentListener.experimentFinishedEvent(this);
  }
}
