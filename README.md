# Simple POS

A mainly educational project to learn vuejs build with vite. Additionally use of tailwind for styling.

### Configuration

The application reads `application.yaml` from its working directory (`/app/application.yaml` in the Docker image)
in addition to the bundled defaults. Besides the datasource, one setting is required:

| Property (environment variable)                       | Default            | Meaning                                                                                                   |
|-------------------------------------------------------|--------------------|-----------------------------------------------------------------------------------------------------------|
| `security.jwt.secret-key` (`SECURITY_JWT_SECRET_KEY`) | none, required     | Signs all login tokens. Use a random value of at least 32 bytes, e.g. `openssl rand -hex 32`. Keep it secret. |
| `security.jwt.expiration-time` (`SECURITY_JWT_EXPIRATION_TIME`) | `3600000` (1 h)    | Lifetime of a login token in milliseconds.                                                                |
| `security.jwt.refresh-expiration-time` (`SECURITY_JWT_REFRESH_EXPIRATION_TIME`) | `2592000000` (30 d) | Lifetime of a refresh token in milliseconds. Every refresh issues a new one, so a session stays alive as long as it is used within this time.                              |
| `security.jwt.issuer` / `security.jwt.audience` (`SECURITY_JWT_ISSUER` / `SECURITY_JWT_AUDIENCE`) | `simple-pos` | Written into every token and checked on every request, so a token signed with the same key for another service is not accepted. |

Changing the secret key logs out every user. Without a key the application does not start. The `local` Spring
profile comes with a key that is only meant for development; compose reads the key from `.env` (see below).

The frontend is served by the application itself. Other origins may call the API only when listed in
`security.cors.allowed-origins` (comma separated); by default none may.

Logins are rate limited per client address. Behind a reverse proxy such as traefik the address is taken from
`X-Forwarded-For` (`server.forward-headers-strategy: native`), but only for connections from internal addresses
(`server.tomcat.remoteip.internal-proxies`: by default private, loopback, link-local, carrier-grade NAT and IPv6
unique-local ranges). Make sure only the proxy can reach the application, or set
`server.tomcat.remoteip.internal-proxies` to the proxy's address; otherwise anything on those networks can claim any
client address.

### Database schema

The schema is managed by Flyway (`backend/src/main/resources/db/migration`); Hibernate only checks that it matches.
On its first start this version records an existing database as being at version 1 and then applies the later
migrations, the first of which drops the unused `authority` table. Before that first start:

- take a backup (`pg_dump`),
- remove any `spring.jpa.hibernate.ddl-auto` from a mounted `application.yaml`, so it does not override the check.

The migration to exact money is one way. It rounds amounts stored with fractions of a cent (cash entries such as
−10.524 become −10.52, prices to cents, the default unit price of an ingredient to four decimals) and replaces the
price per unit of a delivery by what was paid for it; the previous version cannot read the migrated database, so the
only way back is restoring the backup. The same release merges the sale records of a purchase per stock batch
and deletes sale records that belong to no purchase; the previous version cannot sell on the migrated database
either.

A database that was not kept up to date by the previous version (missing columns, an older role constraint) fails the
check at startup or later; compare it with `V1__baseline.sql`, or dump the data and recreate it.

### Running with compose

`compose.yml` (and `backend/compose.yaml` for a database only) reads its secrets from a `.env` file next to it,
which is not committed; copy `.env.example` and fill in random values:

| Variable                  | Meaning                                                                                                   |
|---------------------------|-----------------------------------------------------------------------------------------------------------|
| `POSTGRES_PASSWORD`       | Password of the `postgres` superuser. Only used to set the database up.                                   |
| `SIMPLE_POS_DB_PASSWORD`  | Password of the `simple_pos` role the application connects as. It owns the `simple_pos` database and nothing else. |
| `SECURITY_JWT_SECRET_KEY` | See above.                                                                                                |

The role and database are created when the Postgres volume is initialised for the first time. A volume that
already exists keeps its old setup: either recreate it (`docker compose down -v`, which deletes the data) or create
the role once by hand with
`docker compose exec postgres sh /docker-entrypoint-initdb.d/init-app-role.sh` after `docker compose up` has
recreated the container with the new configuration.

`backend/compose.yaml` reads `backend/.env`, not the one at the repository root; pass `--env-file ../.env` to use the
same file for both. The `local` Spring profile expects its own Postgres on `localhost:5432` with a `simple_pos`
database and reads `SIMPLE_POS_DB_PASSWORD` from the environment; `direnv` loads the root `.env` automatically.

### Resources:
Vue.JS https://vuejs.org/guide/introduction.html
Vue i18n https://vue-i18n.intlify.dev/guide/essentials/started
Tailwind https://tailwindcss.com/docs/installation
Fontawesome Icon Search https://fontawesome.com/icons

https://dbdiagram.io/d/Simple-POS-669902888b4bb5230eb2e9b6
