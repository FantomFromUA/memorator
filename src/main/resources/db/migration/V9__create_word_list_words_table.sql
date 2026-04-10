CREATE TABLE word_list_words (
    word_list_id BIGINT NOT NULL,
    word_id BIGINT NOT NULL,

    PRIMARY KEY (word_list_id, word_id),

    CONSTRAINT fk_word_list_words_list
        FOREIGN KEY (word_list_id)
        REFERENCES word_lists(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_word_list_words_word
        FOREIGN KEY (word_id)
        REFERENCES words(id)
        ON DELETE CASCADE
);
