package ft.sim.experiment;

import ft.sim.simulation.SimulationController;

/**
 * Created by sina on 20/05/2017.
 */
public class Experiment {

  String map;
  private SimulationController simulation;
  private ExperimentListenerInterface experimentListener;

  public Experiment(String map) {
    this.map = map;
  }

  void runFor(ExperimentListenerInterface experimentController) {
    this.experimentListener = experimentController;

    simulation = SimulationController.getInstance(map);
    simulation.setNonInteractve();
    simulation.setExperiment(this);
    simulation.startSimulation();
  }

  public void finished() {
    simulation.kill();
    //NOTE: this is to be executed before experiment is killed
    experimentListener.experimentFinishedEvent(this);
  }
}
