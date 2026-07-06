package by.kazachenko.ejka.additive.repository;

import by.kazachenko.ejka.additive.model.Origin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OriginRepository extends JpaRepository<Origin, Long> {

    boolean existsByType(String type);

}
