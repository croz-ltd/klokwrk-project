#!/usr/bin/env bash
#
# SPDX-License-Identifier: Apache-2.0
#
# Copyright 2020-2024 CROZ d.o.o, the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# This script demonstrates the initialization of a database via init script.
# It should be the same as a script at location klokwrk-project/support/docker/postgres/1-init_db.sh

set -e

psql -v ON_ERROR_STOP=1 <<-EOSQL
    CREATE USER db_migration WITH ENCRYPTED PASSWORD 'db_migration';
    ALTER USER db_migration WITH CREATEROLE;

    CREATE DATABASE cargotracking_booking_query_database;
EOSQL

psql -d cargotracking_booking_query_database -v ON_ERROR_STOP=1 <<-EOSQL
    GRANT ALL PRIVILEGES ON SCHEMA public TO db_migration;
EOSQL
