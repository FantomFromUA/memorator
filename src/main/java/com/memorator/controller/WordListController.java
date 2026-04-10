package com.memorator.controller;

import java.util.List;

import com.memorator.dto.CreateWordListRequest;
import com.memorator.dto.WordListResponse;
import com.memorator.service.WordListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/word-lists")
@RequiredArgsConstructor
public class WordListController {

    private final WordListService wordListService;

    @PostMapping
    public ResponseEntity<WordListResponse> createWordList(@Valid @RequestBody CreateWordListRequest request) {
        WordListResponse response = wordListService.createWordList(request.getUserId(), request.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<WordListResponse>> getWordListsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(wordListService.getWordListsByUserId(userId));
    }
}
