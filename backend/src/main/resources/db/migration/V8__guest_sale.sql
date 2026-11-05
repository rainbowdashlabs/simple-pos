ALTER TABLE cash DROP CONSTRAINT cash_type_check;
ALTER TABLE cash ADD CONSTRAINT cash_type_check CHECK (type IN ('deposit', 'purchase', 'pledge', 'balance', 'other', 'correction', 'sale'));
