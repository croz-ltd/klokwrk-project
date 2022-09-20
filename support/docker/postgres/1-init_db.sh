#!/usr/bin/env bash

# This script demonstrates the initialization of a database via init script.

set -e

psql -v ON_ERROR_STOP=1 <<-EOSQL
    CREATE USER db_migration WITH ENCRYPTED PASSWORD 'db_migration';
    ALTER USER db_migration WITH CREATEROLE;

    CREATE DATABASE cargotracker_booking_query_database;
    GRANT ALL PRIVILEGES ON DATABASE cargotracker_booking_query_database TO db_migration;
EOSQL
