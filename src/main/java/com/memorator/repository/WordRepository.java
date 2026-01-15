package com.memorator.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.memorator.entity.Word;

@Repository
public interface WordRepository extends JpaRepository<Word, Long> {
    List<Word> findByLanguage_Code(String code);
}

