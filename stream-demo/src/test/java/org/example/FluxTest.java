package org.example;

import org.junit.Test;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.UnicastProcessor;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class FluxTest {

    public static void main(String[] args) {
        /*Flux.just("tom", "jack", "allen")
                .filter(s -> s.length() > 3)
                .map(s -> s.concat("@qq.com"))
                .subscribe(System.out::println);*/
        onErrorReturn();
        Flux.just("").subscribe();
    }

    public static void thread() {
        Flux.just("tom")
                .map(s -> {
                    System.out.println("(concat @qq.com) at [" + Thread.currentThread() + "]");
                    return s.concat("@qq.com");
                })
                .publishOn(Schedulers.newSingle("thread-a"))
                .map(s -> {
                    System.out.println("(concat foo) at [" + Thread.currentThread() + "]: " + s);
                    return s.concat("foo");
                })
                .filter(s -> {
                    System.out.println("(startsWith f) at [" + Thread.currentThread() + "]: " + s);
                    return s.startsWith("t");
                })
                .publishOn(Schedulers.newSingle("thread-b"))
                .map(s -> {
                    System.out.println("(to length) at [" + Thread.currentThread() + "]: " + s);
                    return s.length();
                })
                .subscribeOn(Schedulers.newSingle("source"))
                .subscribe((s) -> System.out.println("subscribe [" + Thread.currentThread() + "]: "));
        System.out.println("end....");

    }

    public static void onErrorReturn() {
        AtomicInteger index = new AtomicInteger(0);
        Flux.just(0, 1, 2, 3)
                .map(i -> {
                    index.incrementAndGet();
                    return 1 / i;
                })
                .onErrorReturn(NullPointerException.class, 0)
                .onErrorReturn(e -> index.get() < 2, 3)
                //因为上一个onErrorReturn匹配了条件，所以异常传播被关闭，之后的
                //onErrorReturn不会再被触发
                .onErrorReturn(e -> index.get() < 1, 2)

                //因为异常类型为NumberFormatException，此处应打印1
                .log().subscribe(System.out::println);
    }

    @Bean
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
}
