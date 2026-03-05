package by.kazachenko.ejka.review.service;

import by.kazachenko.ejka.common.dto.response.PageResponse;
import by.kazachenko.ejka.review.dto.request.ReviewRequest;
import by.kazachenko.ejka.review.dto.request.ReviewVoteRequest;
import by.kazachenko.ejka.review.dto.response.ReviewResponse;

import java.util.UUID;

public interface ReviewService {

    PageResponse<ReviewResponse> getAllReviewsByProductId(
            UUID productId,
            Integer offset,
            Integer limit,
            String sortBy,
            String sortDirection
    );

    PageResponse<ReviewResponse> getAllReviewsByAuthorId(
            UUID authorId,
            Integer offset,
            Integer limit,
            String sortBy,
            String sortDirection
    );

    ReviewResponse createReview(ReviewRequest request);

    void deleteReviewById(UUID id);

    void voteForReview(UUID reviewId, ReviewVoteRequest voteRequest);

}
