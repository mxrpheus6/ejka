package by.kazachenko.ejka.review.dto.request;

import java.util.UUID;

public record ReviewRequest(

        UUID productId,

        String content,

        Integer rating

) {

}
