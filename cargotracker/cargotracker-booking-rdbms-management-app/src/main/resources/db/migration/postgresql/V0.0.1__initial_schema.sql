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
  processor_name text NOT NULL,
  segment integer NOT NULL,
  owner text,
  timestamp text NOT NULL,
  token bytea,
  token_type text,

  PRIMARY KEY (processor_name, segment)
);

-- after this point, cargotracker_readonly will be given SELECT grant on all created tables
CREATE USER cargotracker_readonly WITH PASSWORD 'cargotracker_readonly';
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO cargotracker_readonly;

CREATE TABLE booking_offer_summary (
  booking_offer_identifier uuid PRIMARY KEY,

  customer_identifier text NOT NULL,
  customer_type text NOT NULL,

  origin_location_un_lo_code text NOT NULL,
  origin_location_name text NOT NULL,
  origin_location_country_name text NOT NULL,

  destination_location_un_lo_code text NOT NULL,
  destination_location_name text NOT NULL,
  destination_location_country_name text NOT NULL,

  departure_earliest_time timestamptz NOT NULL,
  departure_latest_time timestamptz NOT NULL,
  arrival_latest_time timestamptz NOT NULL,

  commodity_types text[] NOT NULL,
  commodity_total_weight_kg integer NOT NULL,
  commodity_total_container_teu_count numeric(8, 2) NOT NULL,

  inbound_channel_name text NOT NULL,
  inbound_channel_type text NOT NULL,

  first_event_recorded_at timestamptz NOT NULL,
  last_event_recorded_at timestamptz NOT NULL,
  last_event_sequence_number bigint NOT NULL
);
