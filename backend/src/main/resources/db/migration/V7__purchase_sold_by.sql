ALTER TABLE purchase ADD COLUMN sold_by_id integer;
ALTER TABLE purchase ADD CONSTRAINT purchase_sold_by_fk FOREIGN KEY (sold_by_id) REFERENCES users (id);
CREATE INDEX purchase_sold_by_idx ON purchase (sold_by_id);
