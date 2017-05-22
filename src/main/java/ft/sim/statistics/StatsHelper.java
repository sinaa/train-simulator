package ft.sim.statistics;

/**
 * Created by sina on 22/05/2017.
 */
public class StatsHelper {

  public static <I> void log(StatisticsVariable type, I value) {
    StatisticsController in = StatisticsController.getInstance();
    in.record(new StatisticsItem<>(in, type, value));
  }

  public static <I, T> void logFor(StatisticsVariable var, I item, T value) {
    StatisticsController in = StatisticsController.getInstance();
    in.record(new StatisticsItem<>(in, var, item, value));
  }

  public static <I, U> void log(StatisticsVariable var, I value, U auxData) {
    StatisticsController in = StatisticsController.getInstance();
    StatisticsItem item = new StatisticsItem<>(in, var, value);
    item.setAuxData(auxData);
    in.record(item);
  }


  public static <I> void track(StatisticsVariable var, I value) {
    StatisticsController in = StatisticsController.getInstance();
    in.track(var, new StatisticsItem<>(in, var, value));
  }

}
