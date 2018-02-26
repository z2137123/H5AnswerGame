package com.zee.webgame.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class SpringWebSocketConfig extends WebMvcConfigurerAdapter implements WebSocketConfigurer {

    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//        registry.addHandler(webSocketHandler(),"/ws/socketServer.do").addInterceptors(new HandshakeInterceptor());
//        registry.addHandler(webSocketHandler(), "/sockjs/socketServer.do").addInterceptors(new HandshakeInterceptor()).withSockJS();
    }
 
//    @Bean
//    public TextWebSocketHandler webSocketHandler(){
//        return new GameSocketHandle();
//    }

}
