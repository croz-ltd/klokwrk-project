--
-- SPDX-License-Identifier: Apache-2.0
--
-- Copyright 2020-2023 CROZ d.o.o, the original author or authors.
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
  booking_offer_identifier uuid PRIMARY KEY,

  customer_id text NOT NULL,
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

  total_commodity_weight text NOT NULL,
  total_commodity_weight_kg bigint NOT NULL,
  total_container_teu_count numeric(9, 2) NOT NULL,

  inbound_channel_name text NOT NULL,
  inbound_channel_type text NOT NULL,

  first_event_recorded_at timestamptz NOT NULL,
  last_event_recorded_at timestamptz NOT NULL,
  last_event_sequence_number bigint NOT NULL
);

CREATE TABLE booking_offer_summary_commodity_type (
  booking_offer_identifier uuid NOT NULL,
  commodity_type text NOT NULL,
  PRIMARY KEY (booking_offer_identifier, commodity_type),
  CONSTRAINT FK__booking_offer_summary_commodity_type__booking_offer_summary FOREIGN KEY (booking_offer_identifier) REFERENCES booking_offer_summary(booking_offer_identifier)
);

CREATE TABLE booking_offer_details (
  booking_offer_identifier uuid PRIMARY KEY,

  customer_id text NOT NULL,
  details jsonb NOT NULL,

  inbound_channel_name text NOT NULL,
  inbound_channel_type text NOT NULL,
  first_event_recorded_at timestamptz NOT NULL,
  last_event_recorded_at timestamptz NOT NULL,
  last_event_sequence_number bigint NOT NULL
);
