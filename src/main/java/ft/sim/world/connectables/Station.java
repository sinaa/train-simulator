package ft.sim.world.connectables;

import ft.sim.signal.SignalType;
import ft.sim.simulation.Tickable;
import ft.sim.train.Train;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Sina on 27/02/2017.
 */
public class Station implements Connectable, Tickable {

  ConnectableType type = ConnectableType.STATION;
  // in meters
  private int length = 400;
  private final int delay;

  private final int capacity;
  private int reservedCapacity = 0;

  Map<Train, Double> trains = new HashMap<>();
  Set<Train> trainsLeaving = new HashSet<>();


  @Override
  public double getLength() {
    return length;
  }

  public Station(int capacity, int delay) {
    this.capacity = capacity;
    this.delay = delay;
  }

  public void enteredTrain(Train train) {
    trains.put(train, (double) delay);
    train.signal(SignalType.RED);
  }

  public void leftTrain(Train train) {
    trains.remove(train);
  }

  public void tick(double time) {
    for (Entry<Train, Double> trainElement : trains.entrySet()) {
      Train t = trainElement.getKey();
      double delay = trainElement.getValue();
      if (trainsLeaving.contains(t)) {
        continue; // ignore them, they're leaving
      }
      delay -= time;
      if (delay <= 0) {
        t.signal(SignalType.GREEN);
        trainsLeaving.add(t);
      }
      trainElement.setValue(delay);
    }
  }

  public boolean hasCapacity() {
    return trains.size() < capacity - reservedCapacity;
  }

  public boolean reserveCapacity() {
    if (!hasCapacity()) {
      return false;
    }
    reservedCapacity++;
    return true;
  }

  public void unreserveCapacity() {
    reservedCapacity--;
  }

}
