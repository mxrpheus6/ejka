package by.kazachenko.ejka.additive.model;

import by.kazachenko.ejka.additive.model.enums.DangerLevel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "additives")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Additive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30, unique = true, nullable = false)
    private String code;

    @Column(length = 100)
    private String nameRu;

    @Column(length = 100)
    private String nameEn;

    @Column(length = 100)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private DangerLevel dangerLevel;

    @Column(columnDefinition = "TEXT")
    private String warningDescription;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToMany
    @JoinTable(
            name = "additive_origins",
            joinColumns = @JoinColumn(name = "additive_id"),
            inverseJoinColumns = @JoinColumn(name = "origin_id")
    )
    @BatchSize(size = 20)
    @Builder.Default
    private Set<Origin> origins = new HashSet<>();

}
