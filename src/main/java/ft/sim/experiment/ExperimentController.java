package ft.sim.experiment;

import ft.sim.App;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by sina on 20/05/2017.
 */
public class ExperimentController implements ExperimentListenerInterface {

  private static ExperimentController instance;

  private Set<Experiment> experiments = new LinkedHashSet<>();
  private Set<Experiment> completedExperiments = new LinkedHashSet<>();

  private App app;

  public static ExperimentController getInstance() {
    if (instance == null) {
      instance = new ExperimentController();
    }
    return instance;
  }

  private ExperimentController(){
    Experiment experiment = new Experiment();
    experiments.add(experiment);
  }

  public void start() {
    runNext();
  }

  public void experimentFinishedEvent(Experiment experiment) {
    experiments.remove(experiment);
    completedExperiments.add(experiment);
    runNext();
  }

  private void runNext() {
    if (experiments.iterator().hasNext()) {
      experiments.iterator().next().run(this);
    } else {
      finished();
    }
  }

  private void finished() {
    App.experimentCompleted();
  }

}
