//package org.demo;
//
//import org.springframework.integration.annotation.Gateway;
//import org.springframework.integration.annotation.MessagingGateway;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.simp.annotation.SendToUser;
//import org.springframework.stereotype.Controller;
//
//@MessagingGateway
//@Controller
//public interface WebSocketGateway {
//
//    @MessageMapping("/greeting")
//    @SendToUser("/queue/answer")
//    @Gateway(requestChannel = "greetingChannel")
//    String greeting(String payload);
//
//}
