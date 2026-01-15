CREATE INDEX idx_words_language_id
    ON words(language_id);

CREATE INDEX idx_word_translations_target
    ON word_translations(target_word_id);
