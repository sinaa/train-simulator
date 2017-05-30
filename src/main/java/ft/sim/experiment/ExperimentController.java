package ft.sim.experiment;

import ft.sim.App;
import ft.sim.App.AppConfig;
import java.util.LinkedHashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sina on 20/05/2017.
 */
public class ExperimentController implements ExperimentListenerInterface {

  protected static transient Logger logger = LoggerFactory.getLogger(ExperimentController.class);

  private static ExperimentController instance;

  private Set<Experiment> experiments = new LinkedHashSet<>();
  private Set<Experiment> completedExperiments = new LinkedHashSet<>();

  private App app;

  private ExperimentController() {
    if (AppConfig.experimentMaps.isEmpty()) {
      logger.error(
          "Empty list of experiment map configurations provided. Please add experimentMaps by providing: --maps=filename1,filename2,...");
      finished();
      return;
    }
    AppConfig.experimentMaps.forEach(map -> experiments.add(new Experiment(map)));
    logger.info("{} experiment maps added.", AppConfig.experimentMaps.size());
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
