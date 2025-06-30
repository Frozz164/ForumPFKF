-- Добавляем колонку diagnosis в таблицу fundraisings
ALTER TABLE fundraisings ADD COLUMN diagnosis VARCHAR(1000);

-- Создаем таблицу для документов
CREATE TABLE fundraising_documents (
    fundraising_id BIGINT NOT NULL,
    document_url VARCHAR(255) NOT NULL,
    FOREIGN KEY (fundraising_id) REFERENCES fundraisings(id) ON DELETE CASCADE
); 