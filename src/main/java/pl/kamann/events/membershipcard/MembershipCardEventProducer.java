package pl.kamann.events.membershipcard;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import pl.kamann.config.rabbit.RabbitMQConfig;

@Service
@RequiredArgsConstructor
public class MembershipCardEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publishMembershipCardEvent(MembershipCardChangeEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                event
        );
    }
}
