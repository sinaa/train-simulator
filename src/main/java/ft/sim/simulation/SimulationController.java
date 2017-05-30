package ft.sim.simulation;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import ft.sim.experiment.Experiment;
import ft.sim.monitoring.CriticalViolationException;
import ft.sim.monitoring.Oracle;
import ft.sim.statistics.StatisticsController;
import ft.sim.statistics.StatisticsVariable;
import ft.sim.statistics.StatsHelper;
import ft.sim.visualisation.Point;
import ft.sim.visualisation.SignalPoint;
import ft.sim.web.SocketSession;
import ft.sim.world.WorldHandler;
import ft.sim.world.connectables.Connectable;
import ft.sim.world.connectables.Station;
import ft.sim.world.connectables.Track;
import ft.sim.world.journey.Journey;
import ft.sim.world.map.GlobalMap;
import ft.sim.world.map.MapBuilder;
import ft.sim.world.signalling.SignalUnit;
import ft.sim.world.train.Train;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.socket.TextMessage;

/**
 * Created by Sina on 21/02/2017.
 */
public class SimulationController {

  public static final String DEFAULT_MAP = "basic";
  // random seed for disruptor's random generation
  public static final int RANDOM_SEED = 0;
  protected static transient final Logger logger = LoggerFactory
      .getLogger(SimulationController.class);
  // From the point of view of a user, how many ticks we should do per second
  private static final int TICKS_PER_SECOND = 1000;
  // How often to send request to user
  private static final int USER_REFRESH_RATE = 50;
  // how many seconds should the simulation run for (max). Default: 2 days
  private static final long MAX_SIMULATION_DURATION = 2 * 24 * 60 * 60;
  // From the view of the simulation, how much time passed since last tick (in seconds)
  private static final double SECONDS_PER_TICK = 1.0 / 10.0;

  private static SimulationController instance = null;
  // oracle instance
  private Oracle oracle;
  private Experiment experiment = null;
  // world instance
  private GlobalMap world = null;
  // elapsed time/tick during the simulation
  private long ticksElapsed = 0;
  private long timeElapsed = 0;
  private long nanosElapsed = 0;
  private int simulationTimeElapsed = 0;
  // The simulation thread
  private Thread simThread;
  // Is the simulation killed?
  private boolean killed = false;
  // Is the simulation running?
  private boolean isRunning = false;
  //private SocketSession socketSession = null;
  private Set<SocketSession> socketSessions = new HashSet<>();
  private boolean interactiveSimulation = true;
  private boolean simulationCompleted = false;

  private SimulationController(String mapName) {
    logger.info("starting new simulation");
    buildWorld(mapName);
    oracle = new Oracle();
    setSimulatorThread();
  }

  public static SimulationController getInstance(String mapName) {
    if (instance == null) {
      instance = new SimulationController(mapName);
    }
    return instance;
  }

  public static SimulationController getInstance() {
    return instance;
  }

  public static SimulationController newInstance(String mapName) {
    if (instance != null) {
      instance.kill();
      instance = null;
    }
    return getInstance(mapName);
  }

  public void setExperiment(Experiment experiment) {
    this.experiment = experiment;
  }

  public void toggleInteractive() {
    interactiveSimulation = !interactiveSimulation;
    logger.info("Interactive Simulation: {}", interactiveSimulation ? "On" : "Off");
  }

  public void setNonInteractve() {
    interactiveSimulation = false;
  }

  public boolean isKilled() {
    return killed;
  }

  public boolean isRunning() {
    return isRunning;
  }

  private void setSimulatorThread() {
    simThread = new Thread(() -> {
      logger.warn("simulation started!");
      StatsHelper.trackEvent(StatisticsVariable.SIMULATION_STARTED);
      while (!Thread.currentThread().isInterrupted()
          && ticksElapsed * SECONDS_PER_TICK < MAX_SIMULATION_DURATION
          && !simulationCompleted) {
        long startTime = System.nanoTime();
        if (world.getJourneys().values().stream().allMatch(Journey::isJourneyFinished)) {
          simulationCompleted = true;
        }
        // Every 100 ticks check if all trains are stopped and any got NOK (it cannot progress further)
        if (ticksElapsed % 100 == 0 &&
            world.getTrains().values().stream().allMatch(t -> t.getEngine().isStopped()) &&
            world.getTrains().values().stream()
                .anyMatch(t -> t.getEcu().gotNOKRadio() || t.getEcu().nextTrainLikelyBroken())) {
          simulationCompleted = true;
        }
        tick();
        long elapsed = System.nanoTime() - startTime;
        nanosElapsed += elapsed;
        double ms = NANOSECONDS.toMillis(elapsed);
        timeElapsed += ms;

        if (interactiveSimulation) {
          // wait for the remaining time (to match TICKS_PER_SECOND)
          int waitTime = (int) Math.floor((USER_REFRESH_RATE / TICKS_PER_SECOND) - ms);
          if (waitTime > 0) {
            /*try {
              Thread.sleep(waitTime);
            } catch (InterruptedException e) {
              //e.printStackTrace();
              logger.warn("Simulation Stopped");
              Thread.currentThread().interrupt();
            }*/
          }
          // Send stats to user every ...
          if (ticksElapsed % TICKS_PER_SECOND == 0) {
            sendStatistics();
            //new Thread(this::sendStatistics).start();
            simulationTimeElapsed = (int) Math.floor(ticksElapsed * 1.0 / TICKS_PER_SECOND);
          }
        }

      }
      simulationCompleted = true;
      isRunning = false;
      logger.info("Simulation completed!");
      StatsHelper.trackEvent(StatisticsVariable.SIMULATION_STOPPED);
      sendStatistics();
      finish();
    });
  }

  private void finish() {
    if (experiment != null) {
      experiment.finished();
    } else {
      kill();
    }
  }

  private void buildWorld(String mapYaml) {
    try {
      world = MapBuilder.buildNewMap(mapYaml);
    } catch (Throwable t) {
      t.printStackTrace();
      logger.error("!!!! Exception happened while building map: {} \n", t.getMessage());
      throw t;
    }
    //Disruptor disruptor = new Disruptor(RANDOM_SEED);
    //disruptor.disruptTheWorld(world);
    StatisticsController.getInstance(world);
  }

  private void tick() {
    WorldHandler.getInstance(world).tick(SECONDS_PER_TICK);
    ticksElapsed++;
    try {
      oracle.checkState(world, ticksElapsed);
    } catch (CriticalViolationException e) {
      logger.error("Critical Violation detected: {}", e.getMessage());
      sendStatistics();
      kill();
    }
  }

  private void sendStatistics() {
    if (socketSessions.size() == 0) {
      return;
    }

    Map<String, Map<String, String>> rootConnectables = new LinkedHashMap<>();
    Map<String, Point> trackPoints = new LinkedHashMap<>();
    Map<String, Point> stationPoints = new LinkedHashMap<>();
    List<SignalPoint> signalPoints = new ArrayList<>();
    Map<Connectable, Integer> rootIndexes = new HashMap<>();
    int rootIndex = 0;
    for (Connectable c : world.getGraph().getRootConnectables()) {
      rootIndexes.put(c, rootIndex);
      Map<String, String> connectableMap = new LinkedHashMap<>();
      String root = c.toString();
      String before = root;
      Iterator<Connectable> mapIterator = world.getGraph().getIterator(c);
      double length = 0;
      if (c instanceof Station) {
        stationPoints.put(root, new Point(length, c.getLength(), rootIndex));
      } else {
        trackPoints.put(root, new Point(length, c.getLength(), rootIndex));
        Map<Integer, SignalUnit> signals = ((Track) c).getBlockSignals();
        signals.forEach((offset, signalUnit) -> signalPoints.add(
            new SignalPoint(offset, world.getTrackID((Track) c), signalUnit.getStatus())));
      }
      while (mapIterator.hasNext()) {
        Connectable next = mapIterator.next();
        if (next instanceof Station) {
          stationPoints
              .put(next.toString(), new Point(length, length + next.getLength(), rootIndex));
        } else {
          trackPoints.put(next.toString(), new Point(length, length + next.getLength(), rootIndex));
          Map<Integer, SignalUnit> signals = ((Track) next).getBlockSignals();
          signals.forEach((offset, signalUnit) -> signalPoints.add(
              new SignalPoint(offset, world.getTrackID((Track) next), signalUnit.getStatus())));
        }
        length += next.getLength();
        connectableMap.put(before, next.toString());
        before = next.toString();
      }
      rootConnectables.put(root, connectableMap);

      rootIndex++;
    }

    Map<String, Point> trainPoints = new LinkedHashMap<>();
    for (Journey j : world.getJourneys().values()) {
      trainPoints.put(j.getTrain().toString(),
          new Point(j.getTailPositionFromRoot(), j.getHeadPositionFromRoot(),
              rootIndexes.get(j.getJourneyPath().getGraphRootConnectable())));
    }

    Gson gsonBuilder = new Gson();

    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("type", "journeyMap");

    jsonObject.add("world", gsonBuilder.toJsonTree(world));
    jsonObject.add("trainPoints", gsonBuilder.toJsonTree(trainPoints));
    jsonObject.add("trackPoints", gsonBuilder.toJsonTree(trackPoints));
    jsonObject.add("stationPoints", gsonBuilder.toJsonTree(stationPoints));
    jsonObject.add("signalPoints", gsonBuilder.toJsonTree(signalPoints));
    jsonObject.add("violations", gsonBuilder.toJsonTree(oracle.getViolations()));

    jsonObject.addProperty("timeElapsedCalculating", timeElapsed);
    jsonObject.addProperty("nanosElapsed", nanosElapsed);
    jsonObject.addProperty("ticksElapsed", ticksElapsed);
    jsonObject.addProperty("simulationTimeElapsed", ticksElapsed * SECONDS_PER_TICK);
    jsonObject.addProperty("interactive", interactiveSimulation);

    String json = gsonBuilder.toJson(jsonObject);

    sendMessageToAllSockets(json);
  }

  private void sendMessageToAllSockets(String json) {
    Iterator<SocketSession> socketsIterator = socketSessions.iterator();
    while (socketsIterator.hasNext()) {
      SocketSession socketSession = socketsIterator.next();
      try {
        socketSession.getSession().sendMessage(new TextMessage(json));
      } catch (Exception e) {
        e.printStackTrace();
        socketsIterator.remove();
      }
    }
  }

  @Async
  public void startSimulation() {
    simThread.start();
    isRunning = true;
  }

  public void kill() {
    StatisticsController.getOptionalInstance().ifPresent(StatisticsController::saveGzip);
    StatisticsController.getOptionalInstance().ifPresent(StatisticsController::clear);
    sendStatistics();
    simThread.interrupt();
    isRunning = false;
    killed = true;
    socketSessions.clear();
    WorldHandler.endWorld(world);
    world = null;

    instance = null;
  }

  public void setSocketSession(SocketSession socketSession) {
    socketSessions.add(socketSession);
  }

  public void removeSocketSessions(SocketSession socketSession) {
    this.socketSessions.remove(socketSessions);
  }

  @Deprecated
  public void startTrains() {
    List<Journey> journeys = new ArrayList<>(world.getJourneys().values());
    int i = 0;
    for (Journey j : journeys) {
      i++;
      j.getTrain().getEngine().setTargetSpeed(60 + (i * 30));
    }
  }

  @Deprecated
  void displayStatistics() {
    List<Train> trains = new ArrayList<Train>(world.getTrains().values());
    List<Journey> journeys = new ArrayList<Journey>(world.getJourneys().values());

    for (Journey j : journeys) {
      int jouneyID = world.getJourneyID(j);
      logger.info("Journey {}: {}", String.valueOf(jouneyID), j.toString());
    }
  }

  public GlobalMap getWorld() {
    return world;
  }

  public void setWorld(String mapYaml) {
    if (isRunning) {
      throw new UnsupportedOperationException("Cannot set new world when simulation is running");
    }
    buildWorld(mapYaml);
  }
}
