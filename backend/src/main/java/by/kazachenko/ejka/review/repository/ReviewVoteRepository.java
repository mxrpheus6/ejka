package by.kazachenko.ejka.review.repository;

import by.kazachenko.ejka.review.dto.response.UserReviewVoteResponse;
import by.kazachenko.ejka.review.model.ReviewVote;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewVoteRepository extends JpaRepository<ReviewVote, Integer> {

    Optional<ReviewVote> findByUserIdAndReviewId(UUID userId, UUID reviewId);

    @Query("""
        SELECT new by.kazachenko.ejka.review.dto.response.UserReviewVoteResponse(v.review.id, v.isUpvote)
        FROM ReviewVote v
        WHERE v.user.id = :userId AND v.review.product.id = :productId
    """)
    List<UserReviewVoteResponse> findUserVotesByProductId(
            @Param("userId") UUID userId,
            @Param("productId") UUID productId
    );

}
