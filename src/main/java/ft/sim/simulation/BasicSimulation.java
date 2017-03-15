package ft.sim.simulation;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import ft.sim.train.Train;
import ft.sim.web.SocketSession;
import ft.sim.world.map.GlobalMap;
import ft.sim.world.journey.Journey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;

/**
 * Created by Sina on 21/02/2017.
 */
public class BasicSimulation {

  protected Logger logger = LoggerFactory.getLogger(BasicSimulation.class);

  public static GlobalMap world = null;
  // From the point of view of a user, how many ticks we should do per second
  int ticksPerSecond = 300;
  // From the view of the simulation, how much time passed since last tick (in seconds)
  double secondsPerTick = 1.0 / 100.0;

  // How often to send request to user
  int userRefreshRate = 500;

  long ticksElapsed = 0;
  long timeElapsed = 0;

  Thread simThread;

  SocketSession socketSession = null;

  public BasicSimulation() {
    world = new GlobalMap();

    simThread = new Thread(() -> {
      while (!Thread.currentThread().isInterrupted()) {
        long startTime = System.nanoTime();
        for (Map.Entry<Integer, Journey> entry : world.getJourneys().entrySet()) {
          Journey j = entry.getValue();
          j.tick(secondsPerTick);
          j.getJourneyInformation().update(j);
        }
        ticksElapsed++;
        long elapsed = System.nanoTime() - startTime;
        double ms = NANOSECONDS.toMillis(elapsed);
        timeElapsed += ms;
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

        if (ticksElapsed % ticksPerSecond == 0) {
          if (socketSession != null) {

            JsonObject jsonObject = new JsonObject();

            Gson gson = new Gson();
            jsonObject.addProperty("type", "journeyMap");
            //jsonObject.add("journeys", gson.toJsonTree(journeysMap));
            jsonObject.add("world", gson.toJsonTree(world));
            jsonObject.addProperty("timeElapsedCalculating", timeElapsed);
            jsonObject.addProperty("ticksElapsed", ticksElapsed);
            jsonObject.addProperty("simulationTimeElapsed", ticksElapsed * secondsPerTick);


            //String json = gson.toJson(journeysMap);
            String json = gson.toJson(jsonObject);
            //logger.info("JSON: {}", json);
            try {
              socketSession.getSession().sendMessage(new TextMessage(json));
            } catch (Exception e) {
              socketSession = null;
              e.printStackTrace();
            }
          }
          //displayStatistics();
          //logger.info("tick: {}", ticksElapsed);
        }
      }
    });
    simThread.start();
  }

  public void kill() {
    simThread.interrupt();
    world = null;
  }

  public void setSocketSession(SocketSession socketSession) {
    this.socketSession = socketSession;
  }

  public void startTrains() {
    List<Journey> journeys = new ArrayList<Journey>(world.getJourneys().values());
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


}
