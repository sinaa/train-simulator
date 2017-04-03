package ft.sim.world.connectables;

import ft.sim.signalling.SignalType;
import ft.sim.simulation.BasicSimulation;
import ft.sim.simulation.Tickable;
import ft.sim.train.Train;
import ft.sim.world.WorldHandler;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
  Set<Train> trainsEntering = new HashSet<>();


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
    train.signalChange(SignalType.RED);
    trainsEntering.remove(train);
  }

  public void leftTrain(Train train) {
    trains.remove(train);
    trainsLeaving.remove(train);
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
        t.signalChange(SignalType.GREEN);
        trainsLeaving.add(t);
      }
      trainElement.setValue(delay);
    }
  }

  public boolean hasCapacity() {
    Set<Train> union = new HashSet<>(trains.keySet());
    union.addAll(trainsEntering);
    union.addAll(trainsLeaving);
    return capacity - union.size() > 0;
  }

  public boolean reserveCapacity(Train train) {
    if (trainsEntering.contains(train)) {
      return true;
    }
    if (!hasCapacity()) {
      return false;
    }
    trainsEntering.add(train);
    return true;
  }

  @Override
  public String toString() {
    try {
      return "Station-" + WorldHandler.getInstance().getWorld().getStationID(this);
    } catch (Exception e) {
      return super.toString();
    }
  }

}
