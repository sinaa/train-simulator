package ft.sim.statistics;

import ft.sim.world.WorldHandler;
import ft.sim.world.map.GlobalMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by sina on 19/05/2017.
 */
public class StatisticsController {

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

  void record(StatisticsItem stat) {
    stats.add(stat);
  }

  void track(StatisticsVariable var, StatisticsItem statisticsItem) {
    tracks.put(var, statisticsItem);
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
    stat += StatisticsItem.getHeader() + "\\n";
    // stats
    stat += stats.stream().map(Object::toString).collect(Collectors.joining("\\n"));
    // tracks
    stat += tracks.values().stream().map(Object::toString).collect(Collectors.joining("\\n"));

    return stat;
  }

  void clear() {
    instance = null;
  }
}
