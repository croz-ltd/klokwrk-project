#!/bin/bash

# ---------- Configuring environment variables based on values from gradle.properties in the root directory. - START
function prop {
  grep "${1}" ../../gradle.properties | cut -d '=' -f2 | sed 's/ //g'
}

cargotrackerBookingRdbmsManagementAppDockerImageVersion=$(prop 'cargotrackerBookingRdbmsManagementAppDockerImageVersion')
export cargotrackerBookingRdbmsManagementAppDockerImageVersion
# ---------- Configuring environment variables based on values from gradle.properties in the root directory. - END

docker-compose --file docker-compose-rdbms-migration.yml --compatibility up

docker container rm cargotracker-booking-rdbms-management-app
