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

  private ExperimentController() {
    Experiment experiment = new Experiment("basic");
    experiments.add(experiment);
    Experiment e2 = new Experiment("experiment-single-track-debugging-1");
    experiments.add(e2);
  }

  public static ExperimentController getInstance() {
    if (instance == null) {
      instance = new ExperimentController();
    }
    return instance;
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
      experiments.iterator().next().runFor(this);
    } else {
      finished();
    }
  }

  private void finished() {
    new Thread(App::experimentCompleted).start();
  }

}
