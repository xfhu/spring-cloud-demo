//package org.demo;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.reactive.HandlerMapping;
//import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
//import org.springframework.web.reactive.socket.WebSocketHandler;
//import org.springframework.web.reactive.socket.WebSocketSession;
//import reactor.core.publisher.Mono;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Configuration
//public class WebConfig {
//
//    @Bean
//    public HandlerMapping handlerMapping() {
//        Map<String, WebSocketHandler> map = new HashMap<>();
//        map.put("/path", myWebSocketHandler());
//        int order = -1; // before annotated controllers
//
//        return new SimpleUrlHandlerMapping(map, order);
//    }
//
//    @Bean
//    public WebSocketHandler myWebSocketHandler() {
//        return new WebSocketHandler() {
//
//            @Override
//            public Mono<Void> handle(WebSocketSession webSocketSession) {
//                return null;
//            }
//        };
//    }
//}
