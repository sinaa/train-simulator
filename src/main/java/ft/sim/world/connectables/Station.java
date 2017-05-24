package ft.sim.world.connectables;

import static ft.sim.world.signalling.SignalType.GREEN;

import ft.sim.simulation.Tickable;
import ft.sim.statistics.StatisticsVariable;
import ft.sim.statistics.StatsHelper;
import ft.sim.world.WorldHandler;
import ft.sim.world.signalling.SignalController;
import ft.sim.world.signalling.SignalType;
import ft.sim.world.train.Train;
import ft.sim.world.train.TrainObjective;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Sina on 27/02/2017.
 */
public class Station implements Connectable, Tickable {

  protected static transient final Logger logger = LoggerFactory.getLogger(Station.class);
  private final int delay;
  private final int capacity;
  ConnectableType type = ConnectableType.STATION;
  // in meters
  private int length = 400;
  private int reservedCapacity = 0;

  private Map<Train, Double> trains = new LinkedHashMap<>();
  private Set<Train> trainsLeaving = new HashSet<>();
  private Set<Train> trainsEntering = new HashSet<>();

  private Map<Track, SignalController> nextBlockSignalController = new HashMap<>();

  private int stationID = 0;

  public Station(int capacity, int delay) {
    this.capacity = capacity;
    this.delay = delay;
  }

  public void setNextBlockSignalController(SignalController nextBlockSignalController,
      Track nextTrack) {
    this.nextBlockSignalController.put(nextTrack, nextBlockSignalController);
  }

  public void enteredTrain(Train train) {
    if (trains.containsKey(train) || trainsLeaving.contains(train)) {
      return;
    }
    trains.put(train, (double) delay);
    train.signalChange(SignalType.RED);
    train.enteredStation(this);
    trainsEntering.remove(train);

    StatsHelper.logFor(StatisticsVariable.TRAIN_ENTERED_STATION, train, this);
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
    train.leftStation(this);
    logger.warn("{} left {}", train, this);
  }

  public void tick(double time) {
    if (trainsLeaving.size() > 1) {
      throw new IllegalStateException("This shouldn't be possible!");
    }
    for (Entry<Train, Double> trainElement : trains.entrySet()) {
      Train t = trainElement.getKey();
      SignalController signalController = nextBlockSignalController
          .get(t.getEcu().getJourneyPlan().getJourneyPath().getTrackAfterStation(this));
      if (signalController == null) {
        if (t.getEcu().getJourneyPlan().getJourneyPath().getLast() != this) {
          logger.error("SignallController is null (shouldn't be)");
        }
        continue;
      }
      double delay = trainElement.getValue();
      if (trainsLeaving.contains(t)) {
        if (t.getEngine().getObjective() != TrainObjective.PROCEED) {
          t.signalChange(SignalType.GREEN);
        }
        continue; // ignore them, they're leaving
      }
      if (trainsLeaving.size() > 0) {
        // if there are other trains leaving, wait for them to leave
        continue;
      }
      if (signalController.getStatus() == GREEN) {
        delay -= time;
        if (delay <= 0) {
          t.signalChange(SignalType.GREEN);
          trainsLeaving.add(t);
        }
      } else {
        break;
      }

      trainElement.setValue(delay);
      break; // Only do this for one train
    }
  }

  public boolean hasCapacity() {
    return capacity - usedCapacity() > 0;
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

  @Override
  public double getLength() {
    return length;
  }

  public void setLength(int length) {
    this.length = length;
  }

  public int getID() {
    return stationID;
  }

  public void setID(int stationID) {
    if (this.stationID != 0) {
      throw new IllegalStateException("The ID can only be set once!");
    }
    this.stationID = stationID;
  }

  @Override
  public String toString() {
    try {
      return "Station-" + (this.stationID != 0 ? this.stationID
          : WorldHandler.getInstance().getWorld().getStationID(this));
    } catch (Exception e) {
      return super.toString();
    }
  }

  /**
   * @deprecated
   */
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
}
