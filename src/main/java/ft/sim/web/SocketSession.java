package ft.sim.web;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import ft.sim.simulation.BasicSimulation;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
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

  private Logger logger = LoggerFactory.getLogger(SocketSession.class);

  public SocketSession(WebSocketSession session) {
    this.session = session;


  }

  public String getResponse(String message) {

    BasicSimulation sim = BaseController.simulation;

    /*if (message.equals("start trains")) {
      sim.startTrains();
      return "OK";
    }*/

    logger.info("message: {}", message);

    try {
      Type stringStringMap = new TypeToken<Map<String, String>>() {
      }.getType();
      Map<String, String> map = gson.fromJson(message, stringStringMap);

      if (map.containsKey("command")) {
        String command = map.get("command");
        switch (command) {
          case "start trains":
            sim.startTrains();
            return "OK";
          case "stop trains":
            sim.kill();
            return "OK";
          case "get push data":
            sim.setSocketSession(this);
            return "OK";
        }
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
