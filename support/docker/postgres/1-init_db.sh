#!/usr/bin/env bash

# This script demonstrates the initialization of a database via init script.
# It should be the same as a script at location klokwrk-project/modules/bc/cargotracking/asd/booking/lib/cargotracking-booking-test-support-testcontainers/src/main/resources/postgresql/init/1-init_db.sh

set -e

psql -v ON_ERROR_STOP=1 <<-EOSQL
    CREATE USER db_migration WITH ENCRYPTED PASSWORD 'db_migration';
    ALTER USER db_migration WITH CREATEROLE;

    CREATE DATABASE cargotracker_booking_query_database;
EOSQL

psql -d cargotracker_booking_query_database -v ON_ERROR_STOP=1 <<-EOSQL
    GRANT ALL PRIVILEGES ON SCHEMA public TO db_migration;
EOSQL
