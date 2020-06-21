#!/bin/bash

docker-compose --file docker-compose-rdbms-migration.yml --compatibility up

docker container rm cargotracker-booking-rdbms-management-app
