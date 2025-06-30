-- Обновляем таблицу charity_documents
ALTER TABLE charity_documents
ADD COLUMN title VARCHAR(255),
ADD COLUMN description TEXT;

-- Обновляем таблицу fundraising_documents
ALTER TABLE fundraising_documents
ADD COLUMN title VARCHAR(255),
ADD COLUMN description TEXT; 