package by.kazachenko.ejka.review.service.impl;

import by.kazachenko.ejka.common.dto.response.PageResponse;
import by.kazachenko.ejka.common.exception.ExceptionMessages;
import by.kazachenko.ejka.common.exception.cutom.ReviewAlreadyExistsException;
import by.kazachenko.ejka.common.exception.cutom.ReviewNotFoundException;
import by.kazachenko.ejka.common.mapper.PageResponseMapper;
import by.kazachenko.ejka.common.security.SecurityUtils;
import by.kazachenko.ejka.product.model.Product;
import by.kazachenko.ejka.product.repository.ProductRepository;
import by.kazachenko.ejka.review.dto.request.ReviewRequest;
import by.kazachenko.ejka.review.dto.request.ReviewVoteRequest;
import by.kazachenko.ejka.review.dto.response.ReviewResponse;
import by.kazachenko.ejka.review.dto.response.UserReviewVoteResponse;
import by.kazachenko.ejka.review.mapper.ReviewMapper;
import by.kazachenko.ejka.review.model.Review;
import by.kazachenko.ejka.review.model.ReviewVote;
import by.kazachenko.ejka.review.repository.ReviewRepository;
import by.kazachenko.ejka.review.repository.ReviewVoteRepository;
import by.kazachenko.ejka.review.service.ReviewService;
import by.kazachenko.ejka.user.model.User;
import by.kazachenko.ejka.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewVoteRepository reviewVoteRepository;
    private final ReviewMapper reviewMapper;

    private final PageResponseMapper pageResponseMapper;

    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getAllReviewsByProductId(
            UUID productId,
            Integer offset,
            Integer limit,
            String sortBy,
            String sortDirection
    ) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);

        Pageable pageable = PageRequest.of(offset, limit, Sort.by(direction, sortBy));

        Page<ReviewResponse> responsePage = reviewRepository
                .findAllByProductId(productId, pageable)
                .map(reviewMapper::toResponse);

        return pageResponseMapper.toResponse(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getAllReviewsByAuthorId(
            UUID authorId,
            Integer offset,
            Integer limit,
            String sortBy,
            String sortDirection
    ) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);

        Pageable pageable = PageRequest.of(offset, limit, Sort.by(direction, sortBy));

        Page<ReviewResponse> responsePage = reviewRepository
                .findAllByAuthorId(authorId, pageable)
                .map(reviewMapper::toResponse);

        return pageResponseMapper.toResponse(responsePage);
    }

    @Override
    @Transactional
    public ReviewResponse createReview(ReviewRequest request) {
        UUID loggedUserId = securityUtils.getLoggedUserId();

        if (reviewRepository.existsByAuthorIdAndProductId(loggedUserId, request.productId())) {
            throw new ReviewAlreadyExistsException(ExceptionMessages.REVIEW_ALREADY_EXISTS);
        }

        Review review = reviewMapper.toEntity(request);

        User creatorRef = userRepository.getReferenceById(loggedUserId);
        review.setAuthor(creatorRef);

        Product proxyProduct = productRepository.getReferenceById(request.productId());
        review.setProduct(proxyProduct);

        reviewRepository.save(review);

        productRepository.recalculateAndUpdateUserRating(request.productId());

        return reviewMapper.toResponse(review);
    }

    @Override
    @Transactional
    public void deleteReviewById(UUID id) {
        UUID loggedUserId = securityUtils.getLoggedUserId();

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException(ExceptionMessages.REVIEW_NOT_FOUND));

        if (!review.getAuthor().getId().equals(loggedUserId)) {
            throw new AccessDeniedException("Вы не можете удалить чужой отзыв");
        }

        UUID productId = review.getProduct().getId();

        reviewRepository.delete(review);

        productRepository.recalculateAndUpdateUserRating(productId);
    }

    @Override
    @Transactional
    public void voteForReview(UUID reviewId, ReviewVoteRequest voteRequest) {
        UUID userId = securityUtils.getLoggedUserId();

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(ExceptionMessages.REVIEW_NOT_FOUND));

        Optional<ReviewVote> existingVoteOpt = reviewVoteRepository.findByUserIdAndReviewId(userId, reviewId);

        if (existingVoteOpt.isPresent()) {
            ReviewVote existingVote = existingVoteOpt.get();

            if (existingVote.getIsUpvote() == voteRequest.isUpvote()) {
                reviewVoteRepository.delete(existingVote);
            } else {
                existingVote.setIsUpvote(voteRequest.isUpvote());
                reviewVoteRepository.save(existingVote);
            }
        } else {
            ReviewVote newVote = ReviewVote.builder()
                    .user(userRepository.getReferenceById(userId))
                    .review(review)
                    .isUpvote(voteRequest.isUpvote())
                    .build();

            reviewVoteRepository.save(newVote);
        }
    }

    @Override
    public List<UserReviewVoteResponse> getUserUpvotedReviewIds(UUID productId) {
        UUID userId = securityUtils.getLoggedUserId();

        return reviewVoteRepository.findUserVotesByProductId(userId, productId);
    }

}
