package by.kazachenko.ejka.review.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID productId,
        UUID authorId,
        String username,
        String content,
        Integer rating,
        Integer usefulScore,
        Instant createdAt
) {

}
