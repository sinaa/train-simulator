package ft.sim.web;

/**
 * Created by Sina on 27/02/2017.
 */

import ft.sim.App.AppConfig;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
@EnableWebSocket
public class WebSocketConfig extends SpringBootServletInitializer
    implements WebSocketConfigurer {

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    if (AppConfig.isNonInteractive) {
      return;
    }
    registry.addHandler(new WebSocketHandler(), "/ws").withSockJS()
        .setClientLibraryUrl("https://cdn.jsdelivr.net/sockjs/1.1.2/sockjs.min.js");
  }

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(WebSocketConfig.class);
  }

  @Bean
  public ServerEndpointExporter serverEndpointExporter() {
    if (AppConfig.isNonInteractive) {
      return null;
    }
    return new ServerEndpointExporter();
  }
}
