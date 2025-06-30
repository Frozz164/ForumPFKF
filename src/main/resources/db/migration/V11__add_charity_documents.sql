CREATE TABLE charity_documents (
    charity_id BIGINT NOT NULL,
    document_url VARCHAR(255),
    FOREIGN KEY (charity_id) REFERENCES charities(id)
); 