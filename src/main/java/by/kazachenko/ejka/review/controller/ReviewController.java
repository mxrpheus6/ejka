package by.kazachenko.ejka.review.controller;

import by.kazachenko.ejka.common.dto.response.PageResponse;
import by.kazachenko.ejka.review.dto.request.ReviewRequest;
import by.kazachenko.ejka.review.dto.request.ReviewVoteRequest;
import by.kazachenko.ejka.review.dto.response.ReviewResponse;
import by.kazachenko.ejka.review.service.ReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping(params = "productId")
    public ResponseEntity<PageResponse<ReviewResponse>> getAllReviewsByProductId(
            @RequestParam("productId") UUID productId,
            @RequestParam(defaultValue = "0") @Min(0) Integer offset,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        PageResponse<ReviewResponse> reviewsResponse = reviewService.getAllReviewsByProductId(
                productId,
                offset,
                limit,
                sortBy,
                sortDirection
        );

        return ResponseEntity.ok(reviewsResponse);
    }

    @GetMapping(params = "authorId")
    public ResponseEntity<PageResponse<ReviewResponse>>  getAllReviewsByAuthorId(
            @RequestParam("authorId") UUID authorId,
            @RequestParam(defaultValue = "0") @Min(0) Integer offset,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        PageResponse<ReviewResponse> reviewsResponse = reviewService.getAllReviewsByAuthorId(
                authorId,
                offset,
                limit,
                sortBy,
                sortDirection
        );

        return ResponseEntity.ok(reviewsResponse);
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(@RequestBody @Valid ReviewRequest request) {
        ReviewResponse reviewResponse = reviewService.createReview(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reviewResponse);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReviewById(@PathVariable UUID reviewId) {
        reviewService.deleteReviewById(reviewId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{reviewId}/vote")
    public ResponseEntity<Void> voteForReview(@PathVariable UUID reviewId, @RequestBody ReviewVoteRequest voteRequest) {
        reviewService.voteForReview(reviewId, voteRequest);

        return ResponseEntity.noContent().build();
    }

}
