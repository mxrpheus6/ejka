package by.kazachenko.ejka.additive.rabbitmq;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdditivePublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.queues.additive-updates.routing-key}")
    private String routingKey;

    public void sendAdditiveUpdate(AdditiveUpdateEvent event) {
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}