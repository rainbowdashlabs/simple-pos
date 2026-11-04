#!/bin/sh
set -eu

psql -v ON_ERROR_STOP=1 -v app_password="$SIMPLE_POS_DB_PASSWORD" \
    --username "${POSTGRES_USER:-postgres}" --dbname "${POSTGRES_DB:-${POSTGRES_USER:-postgres}}" <<'EOSQL'
CREATE ROLE simple_pos LOGIN PASSWORD :'app_password';
CREATE DATABASE simple_pos OWNER simple_pos;
EOSQL
