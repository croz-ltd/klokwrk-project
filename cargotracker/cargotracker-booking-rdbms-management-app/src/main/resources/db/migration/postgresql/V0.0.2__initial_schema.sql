CREATE TABLE token_entry (
  processor_name VARCHAR(255) NOT NULL,
  segment INTEGER NOT NULL,
  owner VARCHAR(255),
  timestamp VARCHAR(255) NOT NULL,
  token BYTEA,
  token_type VARCHAR(255),

  PRIMARY KEY (processor_name, segment)
);

CREATE SEQUENCE cargo_summary_sequence INCREMENT BY 50 MINVALUE 1 MAXVALUE 9223372036854775807 START WITH 1 NO CYCLE;

CREATE TABLE cargo_summary (
  id bigint NOT NULL,
  aggregate_identifier CHAR(36) NOT NULL,
  aggregate_sequence_number BIGINT NOT NULL,
  destination_location VARCHAR(255) NOT NULL,
  inbound_channel_name VARCHAR(255) NOT NULL,
  inbound_channel_type VARCHAR(255) NOT NULL,
  origin_location VARCHAR(255) NOT NULL,

  PRIMARY KEY (id)
);
