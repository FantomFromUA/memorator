CREATE TABLE word_translations (
    source_word_id BIGINT NOT NULL,
    target_word_id BIGINT NOT NULL,

    PRIMARY KEY (source_word_id, target_word_id),

    CONSTRAINT fk_translation_source
        FOREIGN KEY (source_word_id)
        REFERENCES words(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_translation_target
        FOREIGN KEY (target_word_id)
        REFERENCES words(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_no_self_translation
        CHECK (source_word_id <> target_word_id)
);
