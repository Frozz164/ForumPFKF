-- Удаляем существующие внешние ключи
ALTER TABLE donations DROP CONSTRAINT IF EXISTS fk_donations_fundraising;
ALTER TABLE fundraisings DROP CONSTRAINT IF EXISTS fk_fundraisings_charity;
ALTER TABLE recurring_payments DROP CONSTRAINT IF EXISTS fk_recurring_payments_fundraising;
ALTER TABLE reports DROP CONSTRAINT IF EXISTS fk_reports_fundraising;

-- Добавляем новые внешние ключи с каскадным удалением
ALTER TABLE donations 
    ADD CONSTRAINT fk_donations_fundraising 
    FOREIGN KEY (fundraising_id) 
    REFERENCES fundraisings(id) 
    ON DELETE CASCADE;

ALTER TABLE fundraisings 
    ADD CONSTRAINT fk_fundraisings_charity 
    FOREIGN KEY (charity_id) 
    REFERENCES charities(id) 
    ON DELETE CASCADE;

ALTER TABLE recurring_payments 
    ADD CONSTRAINT fk_recurring_payments_fundraising 
    FOREIGN KEY (fundraising_id) 
    REFERENCES fundraisings(id) 
    ON DELETE CASCADE;

ALTER TABLE reports 
    ADD CONSTRAINT fk_reports_fundraising 
    FOREIGN KEY (fundraising_id) 
    REFERENCES fundraisings(id) 
    ON DELETE CASCADE; 