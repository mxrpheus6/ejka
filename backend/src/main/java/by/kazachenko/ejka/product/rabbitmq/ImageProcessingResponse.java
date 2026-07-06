package by.kazachenko.ejka.product.rabbitmq;

import java.util.List;

public record ImageProcessingResponse(
        String id,
        String objectKey,
        String status,
        String parsedText,
        List<ParsedAdditive> additives,
        List<ParsedAllergen> allergens,
        List<ParsedControversial> controversial,
        String errorMessage
) {}
