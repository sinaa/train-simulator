package ft.sim.world.connectables;

import static ft.sim.world.signalling.SignalType.GREEN;
import static ft.sim.world.signalling.SignalType.RED;

import com.google.common.collect.Sets;
import ft.sim.world.signalling.SignalType;
import ft.sim.world.signalling.SignalUnit;
import ft.sim.world.train.TrainTrail;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by sina on 11/04/2017.
 */
public class ObservableHelper {

  public static boolean anyTrains(Set<Observable> observables) {
    return observables.stream().anyMatch(o -> o instanceof TrainTrail);
  }

  public static boolean allGreen(Set<Observable> observables) {
    return getSignals(observables).stream()
        .allMatch(s -> s.getStatus() == GREEN);
  }

  public static boolean hasBlockSignal(Set<Observable> observables) {
    return getSignals(observables).stream()
        .anyMatch(s -> !s.isDistantSignal());
  }

  public static Set<SignalUnit> getRedSignals(Set<Observable> observables) {
    return getSignalTyped(observables, RED);
  }

  public static Set<SignalUnit> getGreenSignals(Set<Observable> observables) {
    return getSignalTyped(observables, GREEN);
  }

  /**
   * Get observables that are in new (current) set, but not in the old set
   */
  public static Set<Observable> getNewObservables(Set<Observable> old, Set<Observable> current) {
    Set<Observable> currentClone = Sets.newHashSet(current);
    current.removeAll(old);

    return currentClone;
  }

  public static Set<SignalUnit> getNewSignals(Set<Observable> old, Set<Observable> current) {
    return getSignals(getNewObservables(old, current));
  }

  /**
   * Get observables which are in the old set, but not in the new (current) set
   */
  public static Set<Observable> getOldObservables(Set<Observable> old, Set<Observable> current) {
    Set<Observable> oldClone = Sets.newHashSet(old);
    oldClone.removeAll(current);

    return oldClone;
  }

  public static Set<SignalUnit> getOldSignals(Set<Observable> old, Set<Observable> current) {
    return getSignals(getOldObservables(old, current));
  }

  public static Set<SignalUnit> getSignalTyped(Set<Observable> observables, SignalType signalType) {
    return getSignals(observables).stream()
        .filter(s -> s.getStatus() == signalType).collect(Collectors.toSet());
  }

  public static Set<SignalUnit> getSignals(Set<Observable> observables) {
    return observables.stream()
        .filter(o -> o instanceof SignalUnit).map(s -> (SignalUnit) s).collect(Collectors.toSet());
  }

}
