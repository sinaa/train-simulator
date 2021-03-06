package ft.sim;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import ft.sim.experiment.ExperimentController;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
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
    AppConfig.isNonInteractive = Arrays.asList(args).contains("experiment");

    context = new SpringApplicationBuilder(App.class).web(!AppConfig.isNonInteractive).run(args);
    AppConfig.init();
    //context = SpringApplication.run(App.class, args);

    if (AppConfig.isNonInteractive) {
      logger.info("Running in Non-interactive mode");
      ExperimentController.getInstance().start();
    }
  }

  public static void experimentCompleted() {
    context.close();
    //SpringApplication.exit(context, () -> 0);
  }

  @Override
  public void run(ApplicationArguments applicationArguments) throws Exception {
    boolean mapsProvided = applicationArguments.containsOption("maps");
    if (mapsProvided) {
      List<String> maps = applicationArguments.getOptionValues("maps");
      maps.forEach(map -> AppConfig.experimentMaps.addAll(Arrays.asList(map.split(","))));
      List<String> toRemove = new ArrayList<>();
      AppConfig.experimentMaps.forEach(file -> {
        File f = new File(file);
        if (f.isDirectory()) {
          toRemove.add(file);
          List<String> files = Arrays
              .stream(f.listFiles((dir, name) -> name.toLowerCase().endsWith(".yaml")))
              .map(File::getAbsolutePath).collect(Collectors.toList());
          AppConfig.experimentMaps.addAll(files);
        }
      });
      AppConfig.experimentMaps.removeAll(toRemove);
    }
    boolean resultsDir = applicationArguments.containsOption("results");
    if (resultsDir) {
      List<String> outDirs = applicationArguments.getOptionValues("results");
      if (outDirs.size() > 1) {
        throw new IllegalArgumentException("Cannot have more than 1 results directories");
      }
      AppConfig.outputDir = outDirs.get(0);
    }
  }

  /*@Override
  public void run(String... args) throws Exception {
    for(String arg: args)
      logger.warn("arg: {}", arg);
  }*/

  public static class AppConfig {

    public static boolean isNonInteractive = true;
    public static Set<String> experimentMaps = new LinkedHashSet<>();
    public static String outputDir = "./results";

    public static void init() {
      // create output dir
      Path path = Paths.get(AppConfig.outputDir);
      //if directory exists?
      if (!Files.exists(path)) {
        try {
          Files.createDirectories(path);
        } catch (IOException e) {
          //failed to create directory
          logger.error("Failed to create results directory!");
          e.printStackTrace();
        }
      }
    }
  }
}
