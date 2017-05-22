package ft.sim.statistics;

import com.google.common.base.Charsets;
import com.google.common.io.CharSink;
import com.google.common.io.Files;
import ft.sim.App.AppConfig;
import ft.sim.world.WorldHandler;
import ft.sim.world.map.GlobalMap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sina on 19/05/2017.
 */
public class StatisticsController {

  protected static transient Logger logger = LoggerFactory.getLogger(StatisticsController.class);
  private static StatisticsController instance;
  private GlobalMap map;
  private List<StatisticsItem> stats = new ArrayList<>();
  private Map<StatisticsVariable, StatisticsItem> tracks = new LinkedHashMap<>();

  private StatisticsController(GlobalMap map) {
    this.map = map;
  }

  public static StatisticsController getInstance(GlobalMap map) {
    if (instance == null) {
      instance = new StatisticsController(map);
    }
    if (instance.map != map) {
      throw new IllegalStateException("new instance started without collecting previous results");
    }
    return instance;
  }

  public static StatisticsController getInstance() {
    return instance;
  }

  public static Optional<StatisticsController> getOptionalInstance() {
    return Optional.ofNullable(instance);
  }

  void record(StatisticsItem stat) {
    stats.add(stat);
  }

  void track(StatisticsVariable var, StatisticsItem statisticsItem) {
    tracks.put(var, statisticsItem);
  }

  StatisticsItem getTrackedValue(StatisticsVariable var) {
    return tracks.get(var);
  }

  double getTime() {
    return WorldHandler.getInstance(map).getTime();
  }

  long getTick() {
    return WorldHandler.getInstance(map).getTick();
  }

  public String collect() {
    String stat = "";
    // header
    stat += StatisticsItem.getHeader() + "\n";
    // stats
    stat += stats.stream().map(Object::toString).collect(Collectors.joining("\n")) + '\n';
    // tracks
    stat += tracks.values().stream().map(Object::toString).collect(Collectors.joining("\n"));

    return stat;
  }

  public void save() {
    String stats = collect();
    String filename = AppConfig.outputDir + "/" + map.getSimpleFileName() + ".csv";

    // if file exists, remove it
    (new File(filename)).delete();

    CharSink sink = Files.asCharSink(new File(filename), Charsets.UTF_8);
    try {
      sink.write(stats);
    } catch (IOException e) {
      e.printStackTrace();
    }

    logger.info("Exported statistics to: {}", filename);
  }

  public void saveGzip() {
    String stats = collect();
    String filename = AppConfig.outputDir + "/" + map.getSimpleFileName() + ".csv.gz";

    // if file exists, remove it
    (new File(filename)).delete();

    try (FileOutputStream output = new FileOutputStream(filename)) {
      try (Writer writer = new OutputStreamWriter(new GZIPOutputStream(output), "UTF-8")) {
        writer.write(stats);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    logger.info("Exported statistics to: {}", filename);
  }

  public void clear() {
    instance = null;
  }
}
