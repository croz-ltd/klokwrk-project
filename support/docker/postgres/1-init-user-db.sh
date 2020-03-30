#!/usr/bin/env bash

set -e

psql -v ON_ERROR_STOP=1 <<-EOSQL
    CREATE USER cargotracker WITH PASSWORD 'cargotracker';

    CREATE DATABASE cargo_tracker_query_database;
    GRANT ALL PRIVILEGES ON DATABASE cargo_tracker_query_database TO cargotracker;
EOSQL
