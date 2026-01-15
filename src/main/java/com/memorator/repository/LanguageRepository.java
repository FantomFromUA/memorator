package com.memorator.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.memorator.entity.Language;

@Repository
public interface LanguageRepository extends JpaRepository<Language, Long> {
    Optional<Language> findByCode(String code);
}

