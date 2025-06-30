CREATE TABLE charity_categories (
    charity_id BIGINT NOT NULL,
    category VARCHAR(50) NOT NULL,
    PRIMARY KEY (charity_id, category),
    FOREIGN KEY (charity_id) REFERENCES charities(id)
); 