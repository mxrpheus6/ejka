package by.kazachenko.ejka.user.repository;

import by.kazachenko.ejka.user.model.User;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.tokenVersion = u.tokenVersion + 1 WHERE u.email = :email")
    void incrementTokenVersion(String email);

}
