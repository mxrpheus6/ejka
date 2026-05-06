package by.kazachenko.ejka.additive.repository;

import by.kazachenko.ejka.additive.model.Additive;

import by.kazachenko.ejka.additive.specification.AdditiveSpecifications;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AdditiveRepository extends JpaRepository<Additive, Long>,
        JpaSpecificationExecutor<Additive> {

    Optional<Additive> findByCode(String code);
    Page<Additive> findAll(Pageable pageable);
    boolean existsByCode(String code);
}
