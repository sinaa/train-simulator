package ft.sim;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import ft.sim.experiment.ExperimentController;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Hello world!
 */
@SpringBootApplication
@EnableAsync
public class App implements ApplicationRunner {

  private static final transient Logger logger = LoggerFactory.getLogger(App.class);
  private static ConfigurableApplicationContext context = null;

  public static void main(String[] args) {
    context = SpringApplication.run(App.class, args);
    if(AppConfig.isInteractive) {
      logger.info("Running in Non-interactive mode");
      ExperimentController.getInstance().start();
    }
  }


  @Override
  public void run(ApplicationArguments applicationArguments) throws Exception {
    List<String> baseOptions = applicationArguments.getNonOptionArgs();
    AppConfig.isInteractive = baseOptions.contains("experiment");
    /*if (isExperimentMode) {
      logger.info("Running in Non-interactive mode");

      context.close();
      return;
    }
    logger.info("Running in Interactive mode.");*/
    /*logger.warn("getNonOptionArgs: {}", applicationArguments.getNonOptionArgs());
    logger.warn("blah: {}", applicationArguments.getOptionValues("blah"));
    logger.warn("bluh: {}", applicationArguments.getOptionValues("bluh"));
    logger.warn("bluh: {}", applicationArguments.getOptionValues("aaa"));
    logger.warn("getSourceArgs: {}", applicationArguments.getSourceArgs());*/
    //logger.warn("getSourceArgs: {}", applicationArguments.getSourceArgs());
  }

  public static void experimentCompleted() {
    context.close();
    //SpringApplication.exit(context, () -> 0);
  }

  /*@Override
  public void run(String... args) throws Exception {
    for(String arg: args)
      logger.warn("arg: {}", arg);
  }*/

  public static class AppConfig {

    public static boolean isInteractive = true;
  }
}
