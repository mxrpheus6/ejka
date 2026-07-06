package by.kazachenko.ejka.user.model;

import by.kazachenko.ejka.user.model.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String name;

    @Column
    private String avatarKey;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 60)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Builder.Default
    @Column(nullable = false)
    private Integer tokenVersion = 0;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Builder.Default
    @Column
    private Instant registrationDate = Instant.now();

    @Builder.Default
    private Boolean isPremium = false;

    @Column
    private LocalDate premiumUntil;

    @Builder.Default
    private Boolean cancelAtPeriodEnd = false;

    @Column
    private String stripeCustomerId;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isBanned = false;

    @Column
    private String banReason;

}
