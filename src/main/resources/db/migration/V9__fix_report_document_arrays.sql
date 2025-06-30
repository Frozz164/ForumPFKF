-- Сначала удаляем существующие колонки
ALTER TABLE reports DROP COLUMN IF EXISTS document_urls;
ALTER TABLE reports DROP COLUMN IF EXISTS document_descriptions;

-- Создаем колонки заново с правильным типом
ALTER TABLE reports ADD COLUMN document_urls text[];
ALTER TABLE reports ADD COLUMN document_descriptions text[]; 