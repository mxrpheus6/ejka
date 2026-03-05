package by.kazachenko.ejka.review.repository;

import by.kazachenko.ejka.review.model.ReviewVote;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewVoteRepository extends JpaRepository<ReviewVote, Integer> {

    Optional<ReviewVote> findByUserIdAndReviewId(UUID userId, UUID reviewId);

}
