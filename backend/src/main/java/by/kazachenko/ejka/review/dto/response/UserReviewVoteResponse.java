package by.kazachenko.ejka.review.dto.response;

import java.util.UUID;

public record UserReviewVoteResponse(
        UUID reviewId,
        boolean isUpvote
) {

}
