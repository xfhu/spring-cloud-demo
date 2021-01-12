package org.example;

import org.junit.Test;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import org.springframework.cloud.stream.binder.PollableMessageSource;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.TaskExecutor;


public class PolledTest {

    //這個測試有問題
    @Test
    public void samplePollingTest() {
        ApplicationContext context = new SpringApplicationBuilder(SamplePolledConfiguration.class)
                .web(WebApplicationType.NONE)
                .run("--spring.jmx.enabled=false", "--spring.cloud.stream.pollable-source=myDestination");

            OutputDestination destination = context.getBean(OutputDestination.class);
            System.out.println("Message 1: " + new String(destination.receive(0, "myDestination-in-0").getPayload()));
            System.out.println("Message 2: " + new String(destination.receive().getPayload()));
            System.out.println("Message 3: " + new String(destination.receive().getPayload()));
    }

    @Import(TestChannelBinderConfiguration.class)
    @EnableAutoConfiguration
    public static class SamplePolledConfiguration {

/*        @Bean
        public MessageSource<?> source() {
            return () -> new GenericMessage<>("My Own Data " + UUID.randomUUID());
        }*/

        @Bean
        public ApplicationRunner poller(PollableMessageSource polledMessageSource, StreamBridge output, TaskExecutor taskScheduler) {
            return args -> {
                taskScheduler.execute(() -> {
                    for (int i = 0; i < 3; i++) {
                        try {
                            if (!polledMessageSource.poll(m -> {
                                String newPayload = ((String) m.getPayload()).toUpperCase();
                                output.send("myDestination-in-0", newPayload);
                            })) {
                                Thread.sleep(2000);
                            }
                        }
                        catch (Exception e) {
                            // handle failure
                        }
                    }
                });
            };
        }
    }
}
