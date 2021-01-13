package org.example.kafka;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.binder.PollableMessageSource;
import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.integration.StaticMessageHeaderAccessor;
import org.springframework.integration.acks.AcknowledgmentCallback;
import org.springframework.messaging.support.GenericMessage;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.UnicastProcessor;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

@SpringBootApplication
public class KafkaConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(KafkaConsumerApplication.class);
    }

    static AtomicInteger count = new AtomicInteger(0);



    @Bean
    public static Consumer<GenericMessage<String>> consume() {
        return msg -> {
            System.out.println("consume["+count.incrementAndGet()+"]: " + msg.getPayload());
            if(count.longValue() % 2 == 0) {
                StaticMessageHeaderAccessor.getAcknowledgmentCallback(msg).noAutoAck();
                StaticMessageHeaderAccessor.getAcknowledgmentCallback(msg).acknowledge(AcknowledgmentCallback.Status.REQUEUE);
            }
        };
    }

//    @Bean
    public static Function<String,String> uppercase() {
        return msg -> msg.toUpperCase();
    }

    //默认1秒调用一次
//    @Bean
    public static Supplier<String> stringSupplier() {
        return () -> "Hello from Supplier";
    }

//    @Bean
    public Supplier<Flux<String>> stringFluxSupplier() {
        return () -> Flux.fromStream(Stream.generate(new Supplier<String>() {
            @Override
            public String get() {
                try {
                    Thread.sleep(10000);

                } catch (Exception e) {
                    // ignore
                }
                return "Hello from Flux Supplier";
            }
        })).subscribeOn(Schedulers.boundedElastic()).share();
    }

    EmitterProcessor<Person> processor = EmitterProcessor.create();

//    @Bean
    public ApplicationRunner runner() {
        return args -> {
            this.processor.onNext(new Person("foo"));
            this.processor.onNext(new Person("bar"));
        };
    }

//    @Bean
    public Supplier<Flux<Person>> processorSupplier() {
        return () -> this.processor;
    }

//    @Bean
    public static Function<Flux<Integer>, Tuple2<Flux<String>, Flux<String>>> scatter() {
        return flux -> {
            Flux<Integer> connectedFlux = flux.publish().autoConnect(2);
            UnicastProcessor even = UnicastProcessor.create();
            UnicastProcessor odd = UnicastProcessor.create();
            Flux<Integer> evenFlux = connectedFlux.filter(number -> number % 2 == 0).doOnNext(number -> even.onNext("EVEN: " + number));
            Flux<Integer> oddFlux = connectedFlux.filter(number -> number % 2 != 0).doOnNext(number -> odd.onNext("ODD: " + number));

            return Tuples.of(Flux.from(even).doOnSubscribe(x -> evenFlux.subscribe()), Flux.from(odd).doOnSubscribe(x -> oddFlux.subscribe()));
        };
    }

//    @Bean
    public ApplicationRunner poller(PollableMessageSource destIn) {
        return args -> {
            while (true) {
                try {
                    if (!destIn.poll(m -> {
                        String newPayload = ((String)m.getPayload()).toUpperCase();
                        System.out.println("poll: " + newPayload);
//                        destOut.send(new GenericMessage<>(newPayload));
                    }, new ParameterizedTypeReference<String>() {})) {
                        Thread.sleep(1000);
                    }
                }
                catch (Exception e) {
                    // handle failure
                }
            }
        };
    }

//    @Autowired
    private BinderAwareChannelResolver resolver;

//    @Bean
    public ApplicationRunner resolveDestination(BinderAwareChannelResolver resolver) {
        return args -> {
            resolver.resolveDestination("kafka-topic-test").send(new GenericMessage<>("app start"));
        };
    }

    @Data
    public static class Person {
        private String name;

        public Person(String name) {
            this.name = name;
        }
    }
}
