package ft.sim.web;

/**
 * Created by Sina on 27/02/2017.
 */

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class WSHandler extends TextWebSocketHandler {

  private static final transient Logger logger = LoggerFactory.getLogger(WSHandler.class);

  //private final CountDownLatch latch = new CountDownLatch(5);

  private AtomicReference<String> messagePayload;

  public SocketSession ss;

  public WSHandler() {

  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    ss = new SocketSession(session);

    TextMessage message = new TextMessage("Socket session opened!");
    session.sendMessage(message);
    logger.info("Opened new session in instance " + this);
    //boop();
  }

  @Override
  public void handleTextMessage(WebSocketSession session, TextMessage message)
      throws Exception {

    /*Thread thread = new Thread(new Runnable() {
      public void run() {
        boop();
      }
    });
    thread.start();*/

    this.logger.info("Received: " + message);
    String msg = ss.getResponse(message.getPayload());
    logger.info("Replying: " + msg);
    session.sendMessage(new TextMessage(msg));
    //session.close();
    //this.messagePayload.set(message.getPayload());
    //this.latch.countDown();

  }

  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception)
      throws Exception {
    session.close(CloseStatus.SERVER_ERROR);
    logger.info("session closed: " + this);
  }


}