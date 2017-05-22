package ft.sim.statistics;

/**
 * Created by sina on 22/05/2017.
 */
public class StatsHelper {

  public static <I> void log(StatisticsVariable type, I value) {
    StatisticsController in = StatisticsController.getInstance();
    if (in == null) {
      return;
    }
    in.record(new StatisticsItem<>(in, type, value));
  }

  public static <I, T> void logFor(StatisticsVariable var, I item) {
    StatisticsController in = StatisticsController.getInstance();
    if (in == null) {
      return;
    }
    in.record(new StatisticsItem<>(item, in, var));
  }

  public static <I, T> void logFor(StatisticsVariable var, I item, T value) {
    StatisticsController in = StatisticsController.getInstance();
    if (in == null) {
      return;
    }
    in.record(new StatisticsItem<>(in, var, item, value));
  }

  public static <I, U> void log(StatisticsVariable var, I value, U auxData) {
    StatisticsController in = StatisticsController.getInstance();
    if (in == null) {
      return;
    }
    StatisticsItem item = new StatisticsItem<>(in, var, value);
    item.setAuxData(auxData);
    in.record(item);
  }


  public static <I> void track(StatisticsVariable var, I value) {
    StatisticsController in = StatisticsController.getInstance();
    if (in == null) {
      return;
    }
    in.track(var, new StatisticsItem<>(in, var, value));
  }

  public static void trackEvent(StatisticsVariable var) {
    StatisticsController in = StatisticsController.getInstance();
    if (in == null) {
      return;
    }
    in.track(var, new StatisticsItem<>(in, var));
  }

  public static StatisticsItem getStatItem(StatisticsVariable var){
    StatisticsController in = StatisticsController.getInstance();
    if (in == null) {
      return null;
    }
    return in.getTrackedValue(var);
  }

}
