CREATE TABLE report_documents (
    report_id BIGINT NOT NULL,
    document_url VARCHAR(255),
    document_description VARCHAR(500),
    FOREIGN KEY (report_id) REFERENCES reports(id)
); 