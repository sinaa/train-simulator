package ft.sim.web;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import ft.sim.simulation.SimulationController;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * Created by Sina on 27/02/2017.
 */
public class SocketSession {

  private final WebSocketSession session;

  private static final Gson gson = new Gson();

  private static final transient Logger logger = LoggerFactory.getLogger(SocketSession.class);

  public SocketSession(WebSocketSession session) {
    this.session = session;
  }

  public String getResponse(String message) {

    /*if (message.equals("start trains")) {
      sim.startTrains();
      return "OK";
    }*/

    SimulationController simulation = SimulationController.getInstance();
    /*if (simulation.isKilled()) {
      simulation = SimulationController.newInstance();
    }*/
    logger.info("message: {}", message);

    try {
      Type stringStringMap = new TypeToken<Map<String, String>>() {
      }.getType();
      Map<String, String> map = gson.fromJson(message, stringStringMap);

      if (map.containsKey("command")) {
        return CommandHelper.processCommand(simulation, map, this) ? "OK" : "FAIL";
      }
      if (map.containsKey("type") && map.get("type").equals("set")) {
        return CommandHelper.processSetCommand(simulation, map) ? "OK" : "FAIL";
      }
    } catch (com.google.gson.JsonSyntaxException ex) {
      // wasn't json
      ex.printStackTrace();
    }

    return "echo: " + message;
  }

  public WebSocketSession getSession() {
    return session;
  }

  @Deprecated
  void boop() {
    int i = 1;
    while (i < 10) {
      TextMessage m = new TextMessage("boop: " + i);
      try {
        session.sendMessage(m);

      } catch (IOException e) {
        e.printStackTrace();
      }
      i++;
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

}
