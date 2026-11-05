DELETE FROM purchase_part WHERE purchase_id IS NULL;

ALTER TABLE purchase_part ADD COLUMN amount integer;
ALTER TABLE purchase_part ADD COLUMN cost numeric(12, 2);
ALTER TABLE purchase_part ADD COLUMN ingredient_id integer;

UPDATE purchase_part pp
SET amount = merged.units
FROM (SELECT min(id) AS kept, count(*) AS units FROM purchase_part GROUP BY purchase_id, storage_id) merged
WHERE pp.id = merged.kept;
DELETE FROM purchase_part WHERE amount IS NULL;

UPDATE purchase_part pp
SET ingredient_id = s.ingredient_id,
    cost          = round(s.cost * shares.sold_after / s.amount, 2) - round(s.cost * (shares.sold_after - pp.amount) / s.amount, 2)
FROM storage s,
     (SELECT id, sum(amount) OVER (PARTITION BY storage_id ORDER BY id) AS sold_after FROM purchase_part) shares
WHERE s.id = pp.storage_id
  AND shares.id = pp.id;

ALTER TABLE purchase_part ALTER COLUMN amount SET NOT NULL;
ALTER TABLE purchase_part ALTER COLUMN cost SET NOT NULL;
ALTER TABLE purchase_part ALTER COLUMN ingredient_id SET NOT NULL;
ALTER TABLE purchase_part ALTER COLUMN purchase_id SET NOT NULL;
ALTER TABLE purchase_part
    ADD CONSTRAINT fk_purchase_part_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredient (id);

ALTER TABLE purchase ALTER COLUMN purchased SET NOT NULL;
ALTER TABLE storage ALTER COLUMN purchased SET NOT NULL;
