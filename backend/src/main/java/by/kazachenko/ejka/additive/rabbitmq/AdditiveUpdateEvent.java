package by.kazachenko.ejka.additive.rabbitmq;

public record AdditiveUpdateEvent(
        Long additiveId,
        String code,
        ActionType action
) {
}
