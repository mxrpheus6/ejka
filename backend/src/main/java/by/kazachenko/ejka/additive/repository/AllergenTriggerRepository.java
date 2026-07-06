package by.kazachenko.ejka.additive.repository;

import by.kazachenko.ejka.additive.model.AllergenTrigger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AllergenTriggerRepository extends JpaRepository<AllergenTrigger, Long> {

}
