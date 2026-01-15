package com.memorator.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
    name = "words",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"language_id", "value"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @Column(nullable = false)
    private String value;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    @ManyToMany
    @JoinTable(
        name = "word_translations",
        joinColumns = @JoinColumn(name = "source_word_id"),
        inverseJoinColumns = @JoinColumn(name = "target_word_id")
    )
    private Set<Word> translations = new HashSet<>();
}

