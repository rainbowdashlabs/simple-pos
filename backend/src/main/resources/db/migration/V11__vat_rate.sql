CREATE SEQUENCE vat_rate_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE vat_rate
(
    id         integer      NOT NULL,
    name       varchar(100) NOT NULL,
    rate       numeric(5, 2) NOT NULL,
    is_default boolean      NOT NULL DEFAULT false,
    active     boolean      NOT NULL DEFAULT true,
    CONSTRAINT vat_rate_pkey PRIMARY KEY (id),
    CONSTRAINT vat_rate_rate_check CHECK (rate >= 0 AND rate < 100),
    CONSTRAINT vat_rate_default_active_check CHECK (NOT is_default OR active)
);

CREATE UNIQUE INDEX vat_rate_name_key ON vat_rate (lower(name));
CREATE UNIQUE INDEX vat_rate_one_default ON vat_rate (is_default) WHERE is_default;

INSERT INTO vat_rate (id, name, rate, is_default, active)
VALUES (nextval('vat_rate_seq'), 'Standard', 19.00, true, true);
INSERT INTO vat_rate (id, name, rate, is_default, active)
VALUES (nextval('vat_rate_seq'), 'Reduced', 7.00, false, true);

ALTER TABLE ingredient ADD COLUMN vat_rate_id integer REFERENCES vat_rate (id);
CREATE INDEX ingredient_vat_rate_id ON ingredient (vat_rate_id);
