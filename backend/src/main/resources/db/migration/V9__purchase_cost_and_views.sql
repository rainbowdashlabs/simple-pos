ALTER TABLE purchase ADD COLUMN cost numeric(12, 2);
UPDATE purchase p
SET cost = coalesce((SELECT sum(pp.cost) FROM purchase_part pp WHERE pp.purchase_id = p.id), 0);
ALTER TABLE purchase ALTER COLUMN cost SET NOT NULL;
ALTER TABLE purchase ADD CONSTRAINT purchase_cost_check CHECK (cost >= 0);
ALTER TABLE balance ALTER COLUMN account_id SET NOT NULL;

CREATE VIEW account_balances AS
SELECT a.id                                              AS account_id,
       coalesce(d.deposits, 0) - coalesce(p.purchases, 0) AS balance
FROM account a
         LEFT JOIN (SELECT account_id, sum(amount) AS deposits FROM balance GROUP BY account_id) d
                   ON d.account_id = a.id
         LEFT JOIN (SELECT account_id, sum(price * amount) AS purchases
                    FROM purchase
                    WHERE account_id IS NOT NULL
                    GROUP BY account_id) p ON p.account_id = a.id;

CREATE VIEW ingredient_stock AS
SELECT i.id                                                AS ingredient_id,
       cast(coalesce(sum(s.amount - s.sold), 0) AS integer) AS stock
FROM ingredient i
         LEFT JOIN storage s ON s.ingredient_id = i.id AND s.amount > s.sold
GROUP BY i.id;
