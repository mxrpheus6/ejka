package by.kazachenko.ejka.review.repository;

import by.kazachenko.ejka.review.model.Review;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends CrudRepository<Review, UUID> {

    Page<Review> findAllByProductId(UUID productId, Pageable pageable);

    Page<Review> findAllByAuthorId(UUID authorId, Pageable pageable);

    boolean existsByAuthorIdAndProductId(UUID authorId, UUID productId);

}
