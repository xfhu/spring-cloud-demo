//package org.example;
//
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.context.properties.EnableConfigurationProperties;
//import org.springframework.cloud.gateway.route.RouteLocator;
//import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
//import org.springframework.cloud.stream.annotation.EnableBinding;
//import org.springframework.context.annotation.Bean;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//import reactor.core.publisher.UnicastProcessor;
//import reactor.util.function.Tuple2;
//import reactor.util.function.Tuples;
//
//import java.util.function.Consumer;
//import java.util.function.Function;
//
//@SpringBootApplication
////@EnableConfigurationProperties(UriConfiguration.class)
//@RestController
//public class GateWayApplication {
//
//    public static void main(String[] args) {
//        SpringApplication.run(GateWayApplication.class);
//    }
//
//    @Bean
//    public RouteLocator myRoutes(RouteLocatorBuilder builder) {
//        return builder.routes()
//                .route(p -> p
//                        .path("/get")
//                        .filters(f -> f.addRequestHeader("Hello", "World"))
//                        .uri("httpUri"))
//                .route(p -> p
//                        .host("*.hystrix.com")
//                        .uri(""))
//                .build();
//    }
//
//    @RequestMapping("/fallback")
//    public Mono<String> fallback() {
//        return Mono.just("fallback");
//    }
//
//}
//
//
