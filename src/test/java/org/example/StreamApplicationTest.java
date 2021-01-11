package org.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/*@RunWith(SpringRunner.class)
@SpringBootTest(classes = StreamApplication.class)
@Import({TestChannelBinderConfiguration.class})*/
public class StreamApplicationTest {

    @Autowired
    private InputDestination input;

    @Autowired
    private OutputDestination output;

    @Test
    public void contextLoads() {
        input.send(new GenericMessage<byte[]>("hello".getBytes()));
        assertThat(output.receive().getPayload()).isEqualTo("HELLO".getBytes());
//        System.out.println(new String(output.receive().getPayload()));
    }

    @Test
    public void testSingleInputMultiOutput() {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
                TestChannelBinderConfiguration.getCompleteConfiguration(
                        StreamApplication.class))
                .run("--spring.cloud.function.definition=scatter")) {

            InputDestination inputDestination = context.getBean(InputDestination.class);
            OutputDestination outputDestination = context.getBean(OutputDestination.class);

            for (int i = 0; i < 10; i++) {
                inputDestination.send(MessageBuilder.withPayload(String.valueOf(i).getBytes()).build());
            }

            int counter = 0;
            for (int i = 0; i < 5; i++) {
                Message<byte[]> even = outputDestination.receive(0, 0);
                assertThat(even.getPayload()).isEqualTo(("EVEN: " + String.valueOf(counter++)).getBytes());
                Message<byte[]> odd = outputDestination.receive(0, 1);
                assertThat(odd.getPayload()).isEqualTo(("ODD: " + String.valueOf(counter++)).getBytes());
            }


        }
    }

    @Test
    public void testMultipleFunctions() {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
                TestChannelBinderConfiguration.getCompleteConfiguration(
                        StreamApplication.class))
                .run("--spring.cloud.function.definition=uppercase;reverse")) {

            InputDestination inputDestination = context.getBean(InputDestination.class);
            OutputDestination outputDestination = context.getBean(OutputDestination.class);

            Message<byte[]> inputMessage = MessageBuilder.withPayload("Hello".getBytes()).build();
            inputDestination.send(inputMessage, "uppercase-in-0");
            inputDestination.send(inputMessage, "reverse-in-0");

            Message<byte[]> outputMessage = outputDestination.receive(0, "uppercase-out-0");
            assertThat(outputMessage.getPayload()).isEqualTo("HELLO".getBytes());

            outputMessage = outputDestination.receive(0, "reverse-out-0");
            assertThat(outputMessage.getPayload()).isEqualTo("olleH".getBytes());
        }
    }

    @Test
    public void testRouteFunctions() {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
                TestChannelBinderConfiguration.getCompleteConfiguration(
                        StreamApplication.class))
                .run("--spring.cloud.function.routing-expression="
                        + "T(java.lang.Integer).valueOf(new String(payload)) % 2 == 0 ? 'even' : 'odd'")) {

            InputDestination inputDestination = context.getBean(InputDestination.class);
//            Message<byte[]> inputMessage = MessageBuilder.withPayload("123".getBytes()).build();
            inputDestination.send(MessageBuilder.withPayload("1".getBytes()).build());
            inputDestination.send(MessageBuilder.withPayload("2".getBytes()).build());
            inputDestination.send(MessageBuilder.withPayload("3".getBytes()).build());
            inputDestination.send(MessageBuilder.withPayload("4".getBytes()).build());
        }
    }


}
