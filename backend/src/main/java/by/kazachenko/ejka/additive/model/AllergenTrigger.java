package by.kazachenko.ejka.additive.model;

import by.kazachenko.ejka.additive.model.enums.AllergenCategory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "allergen_triggers",
        uniqueConstraints = @UniqueConstraint(columnNames = {"category", "trigger_word"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllergenTrigger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private AllergenCategory category;

    @Column(name = "trigger_word", length = 100, nullable = false)
    private String triggerWord;

}