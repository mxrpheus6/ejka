package by.kazachenko.ejka.product.rabbitmq;

import java.util.List;
import java.util.UUID;

public record ImageProcessingResponse(
        String id,
        String objectKey,
        String status,
        String parsedText,
        List<ParsedAdditive> additives,
        String errorMessage
) {}
