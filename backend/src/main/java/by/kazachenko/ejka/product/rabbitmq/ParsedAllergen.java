package by.kazachenko.ejka.product.rabbitmq;

public record ParsedAllergen(
        String category,
        String matchedText,
        Integer score
) {

}
