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

CREATE ALIAS SLEEP FOR "org.klokwrk.lib.hi.datasourceproxy.springboot.H2Functions.sleep";

CREATE TABLE IF NOT EXISTS person (
  id integer not null auto_increment,
  name varchar(255) not null,
  lastName varchar(255) not null,
  primary key (id)
);

CREATE TABLE IF NOT EXISTS not_so_interesting_person (
  id integer not null auto_increment,
  name varchar(255) not null,
  lastName varchar(255) not null,
  primary key (id)
);

INSERT INTO person (name, lastName) VALUES ('pero', 'perić');
