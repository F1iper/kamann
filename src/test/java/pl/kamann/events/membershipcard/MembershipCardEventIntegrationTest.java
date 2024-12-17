package pl.kamann.events.membershipcard;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.kamann.entities.membershipcard.MembershipCardAction;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@SpringBootTest
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(initializers = MembershipCardEventIntegrationTest.Initializer.class)
class MembershipCardEventIntegrationTest {

    private static final String EXCHANGE_NAME = "test-exchange";
    private static final String QUEUE_NAME = "test-queue";
    private static final String ROUTING_KEY = "test-routing";

    @Container
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3.11-management")
            .withExposedPorts(5672, 15672);

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            rabbitMQContainer.start();

            TestPropertyValues.of(
                    "spring.rabbitmq.host=" + rabbitMQContainer.getHost(),
                    "spring.rabbitmq.port=" + rabbitMQContainer.getMappedPort(5672),
                    "spring.rabbitmq.username=guest",
                    "spring.rabbitmq.password=guest"
            ).applyTo(context.getEnvironment());
        }
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AmqpAdmin amqpAdmin;

    @BeforeAll
    void setupRabbitMQ() {
        DirectExchange exchange = new DirectExchange(EXCHANGE_NAME);
        Queue queue = new Queue(QUEUE_NAME, true);
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);

        amqpAdmin.declareExchange(exchange);
        amqpAdmin.declareQueue(queue);
        amqpAdmin.declareBinding(binding);

        log.info("RabbitMQ setup complete: exchange={}, queue={}, routingKey={}", EXCHANGE_NAME, QUEUE_NAME, ROUTING_KEY);
    }

    @Test
    void shouldSendAndReceiveMembershipCardEvent() throws Exception {
        MembershipCardChangeEvent event = new MembershipCardChangeEvent(
                1L,
                101L,
                MembershipCardAction.RENEW,
                5,
                LocalDateTime.now()
        );

        log.info("Publishing event: {}", event);
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, event);
        log.info("Event published successfully to exchange={} with routingKey={}", EXCHANGE_NAME, ROUTING_KEY);

        log.info("Waiting for message on queue '{}'", QUEUE_NAME);
        Message message = rabbitTemplate.receive(QUEUE_NAME, 5000);
        assertNotNull(message, "Message was not received within the expected time.");

        MembershipCardChangeEvent receivedEvent = objectMapper.readValue(message.getBody(), MembershipCardChangeEvent.class);
        log.info("Deserialized event: {}", receivedEvent);

        assertEquals(event.getMembershipCardId(), receivedEvent.getMembershipCardId(), "MembershipCardId mismatch");
        assertEquals(event.getUserId(), receivedEvent.getUserId(), "UserId mismatch");
        assertEquals(event.getChangeType(), receivedEvent.getChangeType(), "ChangeType mismatch");
        assertEquals(event.getRemainingEntrances(), receivedEvent.getRemainingEntrances(), "RemainingEntrances mismatch");
        assertNotNull(receivedEvent.getTimestamp(), "Timestamp is null");
        log.info("Event successfully verified!");
    }
}
