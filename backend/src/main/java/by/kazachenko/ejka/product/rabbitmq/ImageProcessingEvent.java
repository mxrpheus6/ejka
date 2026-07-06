package by.kazachenko.ejka.product.rabbitmq;

public record ImageProcessingEvent(
        String id,
        String objectKey
) {

}
