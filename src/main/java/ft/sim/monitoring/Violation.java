package ft.sim.monitoring;

/**
 * Created by Sina on 21/03/2017.
 */
public class Violation {

  private final ViolationType type;
  private final ViolationSeverity severity;
  private final long tickTime;
  private final String description;

  public Violation(ViolationType type, ViolationSeverity severity, long ticksTime, String description){
    this.type = type;
    this.severity = severity;
    this.tickTime = ticksTime;
    this.description = description;
  }

  public ViolationType getType() {
    return type;
  }

  public ViolationSeverity getSeverity() {
    return severity;
  }

  @Override
  public String toString() {
    return String.format("%s - [%s]: %s - %s ", tickTime, severity, type, description);
  }
}
