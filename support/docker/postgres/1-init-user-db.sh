#!/usr/bin/env bash

# This script is a part of an example demonstrating the initialization of a database via init script.
# Do note that this script is not strictly necessary, as we can achieve the same effect simply by using "cargotracker" (instead of "postgres") as a user and database name. We are using such a
# strategy (a "cargotracker" for user and database) in Testcontainers tests, for example.

set -e

psql -v ON_ERROR_STOP=1 <<-EOSQL
    CREATE USER cargotracker WITH PASSWORD 'cargotracker';
    ALTER USER cargotracker WITH CREATEROLE;

    CREATE DATABASE cargotracker_booking_query_database;
    GRANT ALL PRIVILEGES ON DATABASE cargotracker_booking_query_database TO cargotracker;
EOSQL
