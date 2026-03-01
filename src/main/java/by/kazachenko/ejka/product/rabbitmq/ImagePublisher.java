package by.kazachenko.ejka.product.rabbitmq;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImagePublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.queues.image-processing.routing-key}")
    private String routingKey;

    public void sendImageToQueue(ImageProcessingEvent event) {
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }

}
