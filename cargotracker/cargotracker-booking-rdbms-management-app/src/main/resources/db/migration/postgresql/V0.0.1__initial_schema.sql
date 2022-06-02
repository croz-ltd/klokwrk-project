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
  processor_name TEXT NOT NULL,
  segment INTEGER NOT NULL,
  owner TEXT,
  timestamp TEXT NOT NULL,
  token BYTEA,
  token_type TEXT,

  PRIMARY KEY (processor_name, segment)
);

-- after this point, cargotracker_readonly will be given SELECT grant on all created tables
CREATE USER cargotracker_readonly WITH PASSWORD 'cargotracker_readonly';
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO cargotracker_readonly;

CREATE TABLE booking_offer_summary (
  booking_offer_identifier UUID PRIMARY KEY,

  customer_identifier TEXT NOT NULL,
  customer_type TEXT NOT NULL,

  origin_location_un_lo_code TEXT NOT NULL,
  origin_location_name TEXT NOT NULL,
  origin_location_country_name TEXT NOT NULL,

  destination_location_un_lo_code TEXT NOT NULL,
  destination_location_name TEXT NOT NULL,
  destination_location_country_name TEXT NOT NULL,

  departure_earliest_time timestamptz NOT NULL,
  departure_latest_time timestamptz NOT NULL,
  arrival_latest_time timestamptz NOT NULL,

  inbound_channel_name TEXT NOT NULL,
  inbound_channel_type TEXT NOT NULL,

  first_event_recorded_at timestamptz NOT NULL,
  last_event_recorded_at timestamptz NOT NULL,
  last_event_sequence_number BIGINT NOT NULL
);
