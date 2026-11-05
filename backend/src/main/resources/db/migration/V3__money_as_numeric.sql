ALTER TABLE product ALTER COLUMN price TYPE numeric(12, 2) USING round(price::numeric, 2);
ALTER TABLE ingredient ALTER COLUMN price TYPE numeric(12, 4) USING round(price::numeric, 4);
ALTER TABLE ingredient ALTER COLUMN pledge TYPE numeric(12, 2) USING round(pledge::numeric, 2);
ALTER TABLE ingredient ALTER COLUMN pledge_container TYPE numeric(12, 2) USING round(pledge_container::numeric, 2);
ALTER TABLE purchase ALTER COLUMN price TYPE numeric(12, 2) USING round(price::numeric, 2);
ALTER TABLE balance ALTER COLUMN amount TYPE numeric(12, 2) USING round(amount::numeric, 2);
ALTER TABLE cash ALTER COLUMN amount TYPE numeric(12, 2) USING round(amount::numeric, 2);

ALTER TABLE storage ADD COLUMN cost numeric(12, 2);
UPDATE storage SET cost = round(price::numeric * amount, 2);
ALTER TABLE storage ALTER COLUMN cost SET NOT NULL;
ALTER TABLE storage DROP COLUMN price;
