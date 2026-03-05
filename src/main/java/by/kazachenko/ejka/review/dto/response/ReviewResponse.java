package by.kazachenko.ejka.review.dto.response;

import java.util.UUID;

public record ReviewResponse(

        UUID id,

        UUID productId,

        UUID authorId,

        String content,

        Integer rating,

        Integer usefulScore

) {

}
