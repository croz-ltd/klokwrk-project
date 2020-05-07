CREATE ALIAS SLEEP FOR "org.klokwrk.lib.datasourceproxy.springboot.H2Functions.sleep";

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
