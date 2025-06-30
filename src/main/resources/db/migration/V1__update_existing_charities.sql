-- Обновляем существующие записи, где registrationNumber is null
UPDATE charities 
SET registration_number = 'TEMP-' || id 
WHERE registration_number IS NULL; 