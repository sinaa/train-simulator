package ft.sim.world.connectables;

import static ft.sim.signalling.SignalType.GREEN;

import ft.sim.monitoring.Oracle;
import ft.sim.signalling.SignalController;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Sina on 27/02/2017.
 */
public class Station implements Connectable, Tickable {

  protected transient final Logger logger = LoggerFactory.getLogger(Station.class);

  ConnectableType type = ConnectableType.STATION;
  // in meters
  private int length = 400;
  private final int delay;

  private final int capacity;
  private int reservedCapacity = 0;

  Map<Train, Double> trains = new HashMap<>();
  Set<Train> trainsLeaving = new HashSet<>();
  Set<Train> trainsEntering = new HashSet<>();

  SignalController nextBlockSignalController;

  public void setNextBlockSignalController(SignalController nextBlockSignalController) {
    this.nextBlockSignalController = nextBlockSignalController;
  }

  @Override
  public double getLength() {
    return length;
  }

  public Station(int capacity, int delay) {
    this.capacity = capacity;
    this.delay = delay;
  }

  public void enteredTrain(Train train) {
    if (trains.containsKey(train) || trainsLeaving.contains(train)) {
      return;
    }
    trains.put(train, (double) delay);
    train.signalChange(SignalType.RED);
    trainsEntering.remove(train);
  }

  @Override
  public void entered(Train train) {
    if (!trainsEntering.contains(train) && !hasCapacity()) {
      throw new IllegalStateException("Train entering station without capacity!");
    }

    trainsEntering.add(train);
    logger.warn("{} entered {}", train, this);
  }

  public void left(Train train) {
    trains.remove(train);
    trainsLeaving.remove(train);
    logger.warn("{} left {}", train, this);
  }

  public void tick(double time) {
    for (Entry<Train, Double> trainElement : trains.entrySet()) {
      Train t = trainElement.getKey();
      if (nextBlockSignalController == null) {
        continue;
      }
      double delay = trainElement.getValue();
      if (trainsLeaving.contains(t)) {
        continue; // ignore them, they're leaving
      }
      delay -= time;
      if (delay <= 0) {
        if (nextBlockSignalController.getStatus() != GREEN) {
          delay += 5; // wait for 5 more seconds
        } else {
          t.signalChange(SignalType.GREEN);
          trainsLeaving.add(t);
        }
      }
      trainElement.setValue(delay);
    }
  }

  public boolean hasCapacity() {
    return capacity - usedCapacity() > 0;
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

  public int usedCapacity() {
    Set<Train> union = new HashSet<>(trains.keySet());
    union.addAll(trainsEntering);
    union.addAll(trainsLeaving);
    return union.size();
  }

  public int getCapacity() {
    return capacity;
  }
}
