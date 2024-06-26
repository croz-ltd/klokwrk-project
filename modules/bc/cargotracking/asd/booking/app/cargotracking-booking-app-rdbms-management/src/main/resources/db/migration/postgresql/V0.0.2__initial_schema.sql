--
-- SPDX-License-Identifier: Apache-2.0
--
-- Copyright 2020-2024 CROZ d.o.o, the original author or authors.
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

CREATE TABLE booking_offer_summary (
  booking_offer_id uuid PRIMARY KEY,

  customer_id text NOT NULL,
  customer_type text NOT NULL,

  origin_location_un_lo_code text,
  origin_location_name text,
  origin_location_country_name text,

  destination_location_un_lo_code text,
  destination_location_name text,
  destination_location_country_name text,

  departure_earliest_time timestamptz,
  departure_latest_time timestamptz,
  arrival_latest_time timestamptz,

  total_commodity_weight text,
  total_commodity_weight_kg bigint,
  total_container_teu_count numeric(9, 2),

  inbound_channel_name text NOT NULL,
  inbound_channel_type text NOT NULL,

  first_event_recorded_at timestamptz NOT NULL,
  last_event_recorded_at timestamptz NOT NULL,
  last_event_sequence_number bigint NOT NULL
);

CREATE TABLE booking_offer_summary_commodity_type (
  booking_offer_id uuid NOT NULL,
  commodity_type text NOT NULL,
  PRIMARY KEY (booking_offer_id, commodity_type),
  CONSTRAINT fk__booking_offer_summary_commodity_type__booking_offer_summary FOREIGN KEY (booking_offer_id) REFERENCES booking_offer_summary(booking_offer_id)
);

CREATE TABLE booking_offer_details (
  booking_offer_id uuid PRIMARY KEY,

  customer_id text NOT NULL,
  details jsonb NOT NULL,

  inbound_channel_name text NOT NULL,
  inbound_channel_type text NOT NULL,
  first_event_recorded_at timestamptz NOT NULL,
  last_event_recorded_at timestamptz NOT NULL,
  last_event_sequence_number bigint NOT NULL
);
