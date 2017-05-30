package ft.sim.experiment;

import ft.sim.simulation.SimulationController;

/**
 * Created by sina on 20/05/2017.
 */
public class Experiment {

  private String map;
  private SimulationController simulation;
  private ExperimentListenerInterface experimentListener;

  public Experiment(String map) {
    this.map = map;
  }

  /**
   * Run an experiment in batch mode for a controller (to report back to)
   *
   * @param experimentController Who should we notify once the experiment is finished?
   */
  public void runFor(ExperimentListenerInterface experimentController) {
    this.experimentListener = experimentController;

    simulation = SimulationController.getInstance(map);
    simulation.setNonInteractve();
    simulation.setExperiment(this);
    simulation.startSimulation();
  }

  /**
   * Listener for when experiment is finished
   */
  public void finished() {
    simulation.kill();
    //NOTE: this is to be executed before experiment is killed
    experimentListener.experimentFinishedEvent(this);
  }
}
