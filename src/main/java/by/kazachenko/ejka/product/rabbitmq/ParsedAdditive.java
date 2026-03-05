package by.kazachenko.ejka.product.rabbitmq;

public record ParsedAdditive(
        String raw,
        Long id,
        String code,
        String nameRu,
        Double score
) {

}
