package ft.sim.statistics;

/**
 * Created by sina on 19/05/2017.
 */
public class StatisticsItem<V, T, U> {

  private double time; // in seconds
  private long tick; // the tick this was recorded
  private StatisticsVariable type; // type of data recorded
  private V value; // the value being recorded
  private T forObject; // For what object is this item being created?
  private U aux; // auxiliary data recorded for this item

  StatisticsItem(StatisticsController controller, StatisticsVariable type) {
    this.time = controller.getTime();
    this.tick = controller.getTick();
    this.type = type;
  }

  StatisticsItem(StatisticsController controller, StatisticsVariable type, V value) {
    this(controller.getTime(), controller.getTick(), type, value);
  }

  StatisticsItem(double time, long tick, StatisticsVariable type, V value) {
    this.time = time;
    this.tick = tick;
    this.type = type;
    this.value = value;
  }

  // different argument order due to generics
  StatisticsItem(T forObject, StatisticsController controller, StatisticsVariable type) {
    this(forObject, controller.getTime(), controller.getTick(), type);
  }

  StatisticsItem(T forObject, double time, long tick, StatisticsVariable type) {
    this.time = time;
    this.tick = tick;
    this.type = type;
    this.forObject = forObject;
  }

  StatisticsItem(StatisticsController controller, StatisticsVariable type, T forObject, V value) {
    this(controller.getTime(), controller.getTick(), type, forObject, value);
  }

  StatisticsItem(double time, long tick, StatisticsVariable type, T forObject, V value) {
    this.time = time;
    this.tick = tick;
    this.type = type;
    this.forObject = forObject;
    this.value = value;
  }

  public static String getHeader() {
    return "time,tick,type,value,obj,aux";
  }

  public void setAuxData(U aux) {
    this.aux = aux;
  }

  public double getTime() {
    return time;
  }

  public long getTick() {
    return tick;
  }

  public StatisticsVariable getType() {
    return type;
  }

  public T getObject() {
    return forObject;
  }

  public V getValue() {
    return value;
  }

  public String getStrinvValue() {
    return value.toString();
  }

  public String toString() {
    return String.format("%.2f,%s,%s,%s", time, tick, type, getValueObjectAuxCsv());
  }

  private String getValueObjectAuxCsv() {
    //TODO: custom actions based on type
    switch (type) {
      default:
        return String.format("%s,%s,%s", value, forObject, aux);
    }
  }
}
