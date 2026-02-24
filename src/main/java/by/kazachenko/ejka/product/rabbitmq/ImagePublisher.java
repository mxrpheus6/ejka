package by.kazachenko.ejka.product.rabbitmq;

import java.util.Map;
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

    public void sendImageToQueue(String imageId, String imageUrl) {
        var payload = Map.of(
                "id", imageId,
                "url", imageUrl
        );

        rabbitTemplate.convertAndSend(exchange, routingKey, payload);
    }

}
