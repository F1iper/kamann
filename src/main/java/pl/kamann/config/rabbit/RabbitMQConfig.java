package pl.kamann.config.rabbit;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "membership-card-exchange";
    public static final String QUEUE_NAME = "membership-card-queue";
    public static final String ROUTING_KEY = "membership-card";

    @Bean
    public DirectExchange membershipCardExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue membershipCardQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public Binding membershipCardBinding(Queue membershipCardQueue, DirectExchange membershipCardExchange) {
        return BindingBuilder.bind(membershipCardQueue).to(membershipCardExchange).with(ROUTING_KEY);
    }
}