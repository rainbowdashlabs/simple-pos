CREATE SEQUENCE invoice_alias_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE invoice_alias
(
    id            integer      NOT NULL,
    text          varchar(200),
    gtin          varchar(20),
    kind          varchar(10)  NOT NULL,
    ingredient_id integer REFERENCES ingredient (id) ON DELETE CASCADE,
    unit          varchar(10),
    last_price    numeric(12, 4),
    last_price_net boolean      NOT NULL DEFAULT false,
    last_used     timestamp(6) with time zone NOT NULL,
    CONSTRAINT invoice_alias_pkey PRIMARY KEY (id),
    CONSTRAINT invoice_alias_named_check CHECK (text IS NOT NULL OR gtin IS NOT NULL),
    CONSTRAINT invoice_alias_kind_check CHECK (kind IN ('ITEM', 'PLEDGE')),
    CONSTRAINT invoice_alias_unit_check CHECK (unit IN ('CONTAINER', 'PIECE')),
    CONSTRAINT invoice_alias_item_check CHECK (kind <> 'ITEM' OR (ingredient_id IS NOT NULL AND unit IS NOT NULL))
);

CREATE UNIQUE INDEX invoice_alias_gtin_key ON invoice_alias (gtin) WHERE gtin IS NOT NULL;
CREATE UNIQUE INDEX invoice_alias_text_key ON invoice_alias (text) WHERE gtin IS NULL;
CREATE INDEX invoice_alias_text_prefix ON invoice_alias (text varchar_pattern_ops);
CREATE INDEX invoice_alias_ingredient_id ON invoice_alias (ingredient_id);
