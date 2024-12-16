package pl.kamann.events.membershipcard;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import pl.kamann.entities.membershipcard.MembershipCardAction;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MembershipCardEventIntegrationTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private MembershipCardEventProducer eventProducer;

    @Value("${membershipcard.history.queue}")
    private String queueName;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSendAndReceiveMembershipCardEvent() throws Exception {
        MembershipCardChangeEvent event = new MembershipCardChangeEvent(
                1L,
                101L,
                MembershipCardAction.RENEW,
                5,
                LocalDateTime.now()
        );

        eventProducer.publishMembershipCardEvent(event);

        Message message = rabbitTemplate.receive(queueName, 5000); // Wait up to 5 seconds to receive a message
        assertNotNull(message);

        MembershipCardChangeEvent receivedEvent = objectMapper.readValue(message.getBody(), MembershipCardChangeEvent.class);

        assertEquals(event.getMembershipCardId(), receivedEvent.getMembershipCardId());
        assertEquals(event.getUserId(), receivedEvent.getUserId());
        assertEquals(event.getChangeType(), receivedEvent.getChangeType());
        assertEquals(event.getRemainingEntrances(), receivedEvent.getRemainingEntrances());
        assertNotNull(receivedEvent.getTimestamp());
    }
}
