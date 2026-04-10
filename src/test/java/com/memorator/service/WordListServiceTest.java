package com.memorator.service;

import com.memorator.dto.WordListResponse;
import com.memorator.entity.User;
import com.memorator.entity.WordList;
import com.memorator.exception.WordListAlreadyExistsException;
import com.memorator.repository.UserRepository;
import com.memorator.repository.WordListRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WordListServiceTest {

    @Mock
    private WordListRepository wordListRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WordListService wordListService;

    private User buildUser() {
        return User.builder().id(1L).email("test@example.com").login("testuser").build();
    }

    @Test
    void createWordList_success() {
        User user = buildUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(wordListRepository.existsByUserIdAndName(1L, "My List")).thenReturn(false);
        when(wordListRepository.save(any(WordList.class))).thenAnswer(inv -> {
            WordList wl = inv.getArgument(0);
            // simulate @PrePersist and generated id
            WordList saved = WordList.builder()
                    .id(10L)
                    .user(wl.getUser())
                    .name(wl.getName())
                    .build();
            return saved;
        });

        WordListResponse response = wordListService.createWordList(1L, "My List");

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getName()).isEqualTo("My List");
        assertThat(response.getWordCount()).isEqualTo(0);
    }

    @Test
    void createWordList_userNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> wordListService.createWordList(99L, "My List"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void createWordList_nameAlreadyExists() {
        User user = buildUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(wordListRepository.existsByUserIdAndName(1L, "Existing")).thenReturn(true);

        assertThatThrownBy(() -> wordListService.createWordList(1L, "Existing"))
                .isInstanceOf(WordListAlreadyExistsException.class);
    }

    @Test
    void getWordListsByUserId_success() {
        User user = buildUser();
        LocalDateTime now = LocalDateTime.now();

        WordList wl1 = WordList.builder().id(1L).user(user).name("List A").createdAt(now).build();
        WordList wl2 = WordList.builder().id(2L).user(user).name("List B").createdAt(now).build();

        when(userRepository.existsById(1L)).thenReturn(true);
        when(wordListRepository.findByUserId(1L)).thenReturn(List.of(wl1, wl2));
        when(wordListRepository.countWordsByWordListId(1L)).thenReturn(3);
        when(wordListRepository.countWordsByWordListId(2L)).thenReturn(0);

        List<WordListResponse> responses = wordListService.getWordListsByUserId(1L);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getName()).isEqualTo("List A");
        assertThat(responses.get(0).getWordCount()).isEqualTo(3);
        assertThat(responses.get(1).getName()).isEqualTo("List B");
        assertThat(responses.get(1).getWordCount()).isEqualTo(0);
    }

    @Test
    void getWordListsByUserId_userNotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> wordListService.getWordListsByUserId(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");
    }
}
