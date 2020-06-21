#!/usr/bin/env bash

set -e

psql -v ON_ERROR_STOP=1 <<-EOSQL
    CREATE USER cargotracker WITH PASSWORD 'cargotracker';

    CREATE DATABASE cargotracker_booking_query_database;
    GRANT ALL PRIVILEGES ON DATABASE cargotracker_booking_query_database TO cargotracker;
EOSQL
