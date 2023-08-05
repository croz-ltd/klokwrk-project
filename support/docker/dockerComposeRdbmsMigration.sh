#!/bin/bash

# ---------- Configuring environment variables based on values from gradle.properties in the root directory. - START
function prop {
  grep "${1}" ../../gradle.properties | cut -d '=' -f2 | sed 's/ //g'
}

cargotrackingBookingRdbmsManagementAppDockerImageVersion=$(prop 'cargotrackingBookingRdbmsManagementAppDockerImageVersion')
export cargotrackingBookingRdbmsManagementAppDockerImageVersion
# ---------- Configuring environment variables based on values from gradle.properties in the root directory. - END

# docker-compose --file docker-compose-rdbms-migration.yml up

# docker container rm cargotracking-booking-app-rdbms-management

### Starting with docker-compose of version 2.3.3 `docker-compose up` command above does not return to the terminal after container exits. I believe it is a bug. Until fixed, the `run` command
### bellow can be used as an alternative
docker-compose --file docker-compose-rdbms-migration.yml run --rm cargotracking-booking-app-rdbms-management
