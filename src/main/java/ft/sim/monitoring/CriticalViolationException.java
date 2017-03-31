package ft.sim.monitoring;

/**
 * Created by Sina on 31/03/2017.
 */
public class CriticalViolationException extends RuntimeException {

  public CriticalViolationException(Violation violation){
    super(violation.toString());
  }

}
