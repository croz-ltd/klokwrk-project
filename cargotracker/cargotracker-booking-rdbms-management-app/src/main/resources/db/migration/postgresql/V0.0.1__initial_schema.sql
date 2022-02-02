--
-- SPDX-License-Identifier: Apache-2.0
--
-- Copyright 2020-2022 CROZ d.o.o, the original author or authors.
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     https://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

CREATE TABLE token_entry (
  processor_name VARCHAR(255) NOT NULL,
  segment INTEGER NOT NULL,
  owner VARCHAR(255),
  timestamp VARCHAR(255) NOT NULL,
  token BYTEA,
  token_type VARCHAR(255),

  PRIMARY KEY (processor_name, segment)
);

-- after this point, cargotracker_readonly will be given SELECT grant on all created tables
CREATE USER cargotracker_readonly WITH PASSWORD 'cargotracker_readonly';
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO cargotracker_readonly;

CREATE TABLE booking_offer_summary (
  booking_offer_identifier UUID PRIMARY KEY,
  row_num BIGSERIAL UNIQUE,

  origin_location VARCHAR(255) NOT NULL,
  destination_location VARCHAR(255) NOT NULL,

  aggregate_version BIGINT NOT NULL,

  inbound_channel_name VARCHAR(255) NOT NULL,
  inbound_channel_type VARCHAR(255) NOT NULL
);
