package com.memorator.service;

import java.util.List;

import com.memorator.dto.WordListResponse;
import com.memorator.entity.User;
import com.memorator.entity.WordList;
import com.memorator.exception.WordListAlreadyExistsException;
import com.memorator.repository.UserRepository;
import com.memorator.repository.WordListRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WordListService {

    private final WordListRepository wordListRepository;
    private final UserRepository userRepository;

    public WordListResponse createWordList(Long userId, String name) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (wordListRepository.existsByUserIdAndName(userId, name)) {
            throw new WordListAlreadyExistsException();
        }

        WordList wordList = WordList.builder()
                .user(user)
                .name(name)
                .build();

        WordList saved = wordListRepository.save(wordList);
        return new WordListResponse(saved.getId(), saved.getName(), saved.getCreatedAt(), 0);
    }

    public List<WordListResponse> getWordListsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found");
        }
        return wordListRepository.findByUserId(userId).stream()
                .map(wl -> new WordListResponse(
                        wl.getId(),
                        wl.getName(),
                        wl.getCreatedAt(),
                        wordListRepository.countWordsByWordListId(wl.getId())))
                .toList();
    }
}
