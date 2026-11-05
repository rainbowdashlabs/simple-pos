CREATE SEQUENCE account_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE balance_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE cash_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE category_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE ingredient_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE product_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE purchase_part_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE purchase_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE recipe_entry_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE recipe_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE storage_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE token_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE user_preferences_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE users_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE users
(
    id       integer NOT NULL,
    enabled  boolean NOT NULL,
    password character varying(255),
    username character varying(255),
    role     character varying(255),
    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT ukr43af9ap4edm43mmtq01oddj6 UNIQUE (username),
    CONSTRAINT users_role_check CHECK (role IN ('ADMIN', 'STAFF', 'USER'))
);

CREATE TABLE authority
(
    id        integer NOT NULL,
    authority character varying(255),
    user_id   integer,
    CONSTRAINT authority_pkey PRIMARY KEY (id),
    CONSTRAINT fkka37hl6mopj61rfbe97si18p8 FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE user_preferences
(
    id          integer NOT NULL,
    color_theme character varying(255),
    dark_mode   character varying(255),
    locale      character varying(255),
    user_id     integer,
    CONSTRAINT user_preferences_pkey PRIMARY KEY (id),
    CONSTRAINT ukqy8dkrkc8b34dcgwoq2km43rd UNIQUE (user_id),
    CONSTRAINT fkepakpib0qnm82vmaiismkqf88 FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE token
(
    id      integer NOT NULL,
    token   character varying(255),
    user_id integer,
    expires timestamp(6) with time zone,
    CONSTRAINT token_pkey PRIMARY KEY (id),
    CONSTRAINT fkj8rfw4x0wjjyibfqq566j4qng FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE account
(
    id      integer NOT NULL,
    created timestamp(6) with time zone,
    name    character varying(255),
    deleted boolean NOT NULL DEFAULT false,
    CONSTRAINT account_pkey PRIMARY KEY (id)
);

CREATE TABLE balance
(
    id         integer          NOT NULL,
    added      timestamp(6) with time zone,
    amount     double precision NOT NULL,
    account_id integer,
    CONSTRAINT balance_pkey PRIMARY KEY (id),
    CONSTRAINT fkxhe1wv9gspc3wl0w66bptmgs FOREIGN KEY (account_id) REFERENCES account (id)
);

CREATE TABLE cash
(
    id      integer          NOT NULL,
    amount  double precision NOT NULL,
    created timestamp(6) with time zone,
    note    character varying(255),
    type    character varying(255),
    user_id integer,
    CONSTRAINT cash_pkey PRIMARY KEY (id),
    CONSTRAINT fk1wp8eedfah9xgn0hxump81d6g FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE category
(
    id   integer                NOT NULL,
    name character varying(255) NOT NULL,
    CONSTRAINT category_pkey PRIMARY KEY (id)
);

CREATE TABLE ingredient
(
    id               integer                NOT NULL,
    container_size   integer                NOT NULL,
    min_stock        integer                NOT NULL,
    name             character varying(255) NOT NULL,
    pledge           double precision       NOT NULL,
    pledge_container double precision       NOT NULL,
    price            double precision       NOT NULL,
    category_id      integer,
    active           boolean DEFAULT true,
    CONSTRAINT ingredient_pkey PRIMARY KEY (id),
    CONSTRAINT fkd3jvt6c0xji9cvxvckf9aapsa FOREIGN KEY (category_id) REFERENCES category (id)
);

CREATE TABLE product
(
    id          integer                NOT NULL,
    active      boolean                NOT NULL,
    name        character varying(255) NOT NULL,
    price       double precision       NOT NULL,
    category_id integer,
    deleted     boolean DEFAULT false,
    CONSTRAINT product_pkey PRIMARY KEY (id),
    CONSTRAINT fk1mtsbur82frn64de7balymq9s FOREIGN KEY (category_id) REFERENCES category (id)
);

CREATE TABLE recipe
(
    id         integer NOT NULL,
    product_id integer,
    CONSTRAINT recipe_pkey PRIMARY KEY (id),
    CONSTRAINT ukl41nww74r8c9x1fuu2bnecbu9 UNIQUE (product_id),
    CONSTRAINT fk18huu5mjhhqv6iug8wn5o9juo FOREIGN KEY (product_id) REFERENCES product (id)
);

CREATE TABLE recipe_entry
(
    id            bigint  NOT NULL,
    amount        integer NOT NULL,
    ingredient_id integer,
    recipe_id     integer,
    CONSTRAINT recipe_entry_pkey PRIMARY KEY (id),
    CONSTRAINT fkg446qg5w9xfw7ga3ukp9rij41 FOREIGN KEY (ingredient_id) REFERENCES ingredient (id),
    CONSTRAINT fk7osob0iq2l5nqpkrmwnu8cc7h FOREIGN KEY (recipe_id) REFERENCES recipe (id)
);

CREATE TABLE storage
(
    id            integer          NOT NULL,
    amount        integer          NOT NULL,
    price         double precision NOT NULL,
    purchased     timestamp(6) with time zone,
    sold          integer          NOT NULL,
    ingredient_id integer,
    CONSTRAINT storage_pkey PRIMARY KEY (id),
    CONSTRAINT fkg751xn2ev5ea3to21ek9bi2e6 FOREIGN KEY (ingredient_id) REFERENCES ingredient (id)
);

CREATE TABLE purchase
(
    id         integer          NOT NULL,
    amount     integer          NOT NULL,
    price      double precision NOT NULL,
    purchased  timestamp(6) with time zone,
    account_id integer,
    product_id integer,
    CONSTRAINT purchase_pkey PRIMARY KEY (id),
    CONSTRAINT fk3s4jktret4nl7m8yhfc8mfrn5 FOREIGN KEY (product_id) REFERENCES product (id),
    CONSTRAINT fkj4nqpwiw7s85ps78eghvnlaeh FOREIGN KEY (account_id) REFERENCES account (id)
);

CREATE TABLE purchase_part
(
    id          integer NOT NULL,
    purchase_id integer,
    storage_id  integer,
    CONSTRAINT purchase_part_pkey PRIMARY KEY (id),
    CONSTRAINT fkgn8pmnxdgm7i62aeda1r6eaq FOREIGN KEY (purchase_id) REFERENCES purchase (id),
    CONSTRAINT fkrqm1darhglopyrerohxthhw25 FOREIGN KEY (storage_id) REFERENCES storage (id)
);
