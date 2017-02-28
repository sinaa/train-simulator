package ft.sim.simulation;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import ft.sim.train.Train;
import ft.sim.web.SocketSession;
import ft.sim.world.GlobalMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;

/**
 * Created by Sina on 21/02/2017.
 */
public class BasicSimulation {

  protected Logger logger = LoggerFactory.getLogger(BasicSimulation.class);

  GlobalMap world;
  // From the point of view of a user, how many ticks we should do per second
  int ticksPerSecond = 120;
  // From the view of the simulation, how much time passed since last tick (in seconds)
  double secondsPerTick = 1.0 / ticksPerSecond;

  long ticksElapsed = 0;

  Thread simThread;

  SocketSession socketSession = null;

  public BasicSimulation() {
    world = new GlobalMap();
    createJourneys();

    simThread = new Thread(new Runnable() {
      public void run() {
        while (!Thread.currentThread().isInterrupted()) {
          long startTime = System.nanoTime();
          for (Map.Entry<Integer, Journey> entry : journeysMap.entrySet()) {
            Journey j = entry.getValue();
            j.tick(secondsPerTick);
          }
          ticksElapsed++;
          long elapsed = System.nanoTime() - startTime;
          double ms = NANOSECONDS.toMillis(elapsed);
          int waitTime = (int) Math.floor((1000.0 / ticksPerSecond) - ms);
          if (waitTime > 0) {
            try {
              Thread.sleep(waitTime);
            } catch (InterruptedException e) {
              e.printStackTrace();
              Thread.currentThread().interrupt();
            }
          }

          if (ticksElapsed % ticksPerSecond == 0) {
            if (socketSession != null) {

              Gson gson = new Gson();
              String json = gson.toJson(journeysMap);
              logger.info("JSON: {}", json);
              try {
                socketSession.getSession().sendMessage(new TextMessage(json));
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
            displayStatistics();
            //logger.info("tick: {}", ticksElapsed);
          }

        }
      }
    });
    simThread.start();
  }

  public void kill() {
    simThread.interrupt();
  }

  public void setSocketSession(SocketSession socketSession) {
    this.socketSession = socketSession;
  }

  BiMap<Integer, Journey> journeysMap = HashBiMap.create();

  void createJourneys() {
    JourneyPath jp1 = world.getJourneyPaths().get(1);
    Train t1 = world.getTrains().get(1);
    Journey j1 = new Journey(jp1, t1, true);

    JourneyPath jp2 = world.getJourneyPaths().get(2);
    Train t2 = world.getTrains().get(2);
    Journey j2 = new Journey(jp2, t2, true);

    journeysMap.put(1, j1);
    journeysMap.put(2, j2);
  }

  public void startTrains() {
    List<Journey> journeys = new ArrayList<Journey>(journeysMap.values());
    for (Journey j : journeys) {
      j.getTrain().getEngine().setTargetSpeed(60);
    }
  }

  int getJourneyID(Journey j) {
    return journeysMap.inverse().get(j);
  }

  void displayStatistics() {
    List<Train> trains = new ArrayList<Train>(world.getTrains().values());
    List<Journey> journeys = new ArrayList<Journey>(journeysMap.values());

    for (Journey j : journeys) {
      int jouneyID = getJourneyID(j);
      logger.info("Journey {}: {}", String.valueOf(jouneyID), j.toString());
    }
  }


}
