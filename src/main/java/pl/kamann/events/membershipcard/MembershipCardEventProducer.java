package pl.kamann.events.membershipcard;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MembershipCardEventProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${membershipcard.history.exchange}")
    private String exchange;

    @Value("${membershipcard.history.routingkey}")
    private String routingKey;

    public void publishMembershipCardEvent(MembershipCardChangeEvent event) {
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}
