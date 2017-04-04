package ft.sim.simulation;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.org.apache.xpath.internal.operations.Or;
import ft.sim.monitoring.CriticalViolationException;
import ft.sim.train.Train;
import ft.sim.web.SocketSession;
import ft.sim.world.WorldHandler;
import ft.sim.world.map.GlobalMap;
import ft.sim.world.journey.Journey;
import ft.sim.world.map.MapBuilder;
import ft.sim.monitoring.Oracle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
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
public class BasicSimulation {

  protected Logger logger = LoggerFactory.getLogger(BasicSimulation.class);

  // random seed for disruptor's random generation
  public static final int RANDOM_SEED = 0;

  // world instance
  private GlobalMap world = null;

  // From the point of view of a user, how many ticks we should do per second
  int ticksPerSecond = 300;
  // From the view of the simulation, how much time passed since last tick (in seconds)
  private double secondsPerTick = 1.0 / 100.0;

  // How often to send request to user
  private int userRefreshRate = 500;

  // elapsed time/tick during the simulation
  private long ticksElapsed = 0;
  private long timeElapsed = 0;
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

  private static BasicSimulation instance = null;

  // how many seconds should the simulation run for (max). Default: 2 days
  private long simulationDuration = 2 * 24 * 60 * 60;

  private boolean simulationCompleted = false;

  public static final String DEFAULT_MAP = "basic";

  // oracle instance
  Oracle oracle;

  public static BasicSimulation getInstance() {
    if (instance == null) {
      instance = new BasicSimulation();
    }
    return instance;
  }

  public void toggleInteractive() {
    interactiveSimulation = !interactiveSimulation;
    logger.info("Interactive Simulation: {}", interactiveSimulation ? "On" : "Off");
  }

  public boolean isKilled() {
    return killed;
  }

  public boolean isRunning() {
    return isRunning;
  }

  public static BasicSimulation newInstance() {
    if (instance != null) {
      instance.kill();
      instance = null;
    }
    return getInstance();
  }

  private BasicSimulation() {
    logger.info("starting new simulation");
    buildWorld(DEFAULT_MAP);
    oracle = new Oracle();
    setSimulatorThread();
  }

  private void setSimulatorThread() {
    simThread = new Thread(() -> {
      while (!Thread.currentThread().isInterrupted()
          && ticksElapsed * secondsPerTick < simulationDuration
          && !simulationCompleted) {
        long startTime = System.nanoTime();
        tick();
        long elapsed = System.nanoTime() - startTime;
        double ms = NANOSECONDS.toMillis(elapsed);
        timeElapsed += ms;

        if (interactiveSimulation) {
          // wait for the remaining time (to match ticksPerSecond)
          int waitTime = (int) Math.floor((userRefreshRate / ticksPerSecond) - ms);
          if (waitTime > 0) {
            try {
              Thread.sleep(waitTime);
            } catch (InterruptedException e) {
              //e.printStackTrace();
              logger.warn("Simulation Stopped");
              Thread.currentThread().interrupt();
            }
          }
          // Send stats to user every ...
          if (ticksElapsed % ticksPerSecond == 0) {
            sendStatistics();
            simulationTimeElapsed = (int) Math.floor(ticksElapsed * 1.0 / ticksPerSecond);
          }
        }
      }
      simulationCompleted = true;
      isRunning = false;
      logger.info("Simulation completed!");
    });
  }

  private void buildWorld(String mapYaml) {
    try {
      world = MapBuilder.buildNewMap(mapYaml);
    } catch (Throwable t) {
      t.printStackTrace();
      logger.error("!!!! Exception happened while building map: {} \n", t.getMessage());
      throw t;
    }
    Disruptor disruptor = new Disruptor(RANDOM_SEED);
    disruptor.disruptTheWorld(world);
  }

  public void setWorld(String mapYaml) {
    if (isRunning) {
      throw new UnsupportedOperationException("Cannot set new world when simulation is running");
    }
    buildWorld(mapYaml);
  }

  private void tick() {
    WorldHandler.getInstance(world).tick(secondsPerTick);
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

    Gson gsonBuilder = new Gson();

    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("type", "journeyMap");
    //jsonObject.add("journeys", gson.toJsonTree(journeysMap));
    jsonObject.add("world", gsonBuilder.toJsonTree(world));
    jsonObject.addProperty("timeElapsedCalculating", timeElapsed);
    jsonObject.addProperty("ticksElapsed", ticksElapsed);
    jsonObject.addProperty("simulationTimeElapsed", ticksElapsed * secondsPerTick);
    jsonObject.addProperty("interactive", interactiveSimulation);
    jsonObject.add("violations", gsonBuilder.toJsonTree(oracle.getViolations()));
    //String json = gson.toJson(journeysMap);
    String json = gsonBuilder.toJson(jsonObject);
    //logger.info("JSON: {}", json);.
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
    simThread.interrupt();
    sendStatistics();
    isRunning = false;
    killed = true;
    socketSessions.clear();
    WorldHandler.endWorld(world);
    world = null;
  }

  public void setSocketSession(SocketSession socketSession) {
    socketSessions.add(socketSession);
  }

  public void removeSocketSessions(SocketSession socketSession) {
    this.socketSessions.remove(socketSessions);
  }

  public void startTrains() {
    List<Journey> journeys = new ArrayList<>(world.getJourneys().values());
    int i = 0;
    for (Journey j : journeys) {
      i++;
      j.getTrain().getEngine().setTargetSpeed(60 + (i * 30));
    }
  }

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
}
