ALTER TABLE cash DROP CONSTRAINT cash_type_check;
ALTER TABLE cash ADD CONSTRAINT cash_type_check CHECK (type IN ('deposit', 'purchase', 'pledge', 'balance', 'other', 'correction'));

CREATE SEQUENCE till_count_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE till_count
(
    id            integer                  NOT NULL,
    counted       numeric(12, 2)           NOT NULL,
    expected      numeric(12, 2)           NOT NULL,
    denominations jsonb                    NOT NULL,
    counted_by_id integer                  NOT NULL,
    created       timestamp(6) with time zone NOT NULL,
    CONSTRAINT till_count_pkey PRIMARY KEY (id),
    CONSTRAINT till_count_counted_by_fk FOREIGN KEY (counted_by_id) REFERENCES users (id),
    CONSTRAINT till_count_counted_check CHECK (counted >= 0)
);

CREATE INDEX till_count_created_idx ON till_count (created);
CREATE INDEX till_count_counted_by_idx ON till_count (counted_by_id);
