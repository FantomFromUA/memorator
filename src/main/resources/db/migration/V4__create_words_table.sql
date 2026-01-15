CREATE TABLE words (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    language_id BIGINT NOT NULL,
    value VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_words_language
        FOREIGN KEY (language_id)
        REFERENCES languages(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_word_language_value
        UNIQUE (language_id, value)
);
