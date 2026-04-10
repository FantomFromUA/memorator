package com.memorator.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.memorator.entity.WordList;

@Repository
public interface WordListRepository extends JpaRepository<WordList, Long> {
    boolean existsByUserIdAndName(Long userId, String name);
    List<WordList> findByUserId(Long userId);

    @Query("SELECT SIZE(wl.words) FROM WordList wl WHERE wl.id = :id")
    int countWordsByWordListId(@Param("id") Long id);
}
