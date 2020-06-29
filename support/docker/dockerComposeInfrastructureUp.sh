#!/bin/bash

serviceName=$1

# ---------- Configuring environment variables based on values from gradle.properties in the root directory. - START
function prop {
  grep "${1}" ../../gradle.properties | cut -d '=' -f2 | sed 's/ //g'
}

postgreSqlDockerImageVersion=$(prop 'postgreSqlDockerImageVersion')
export postgreSqlDockerImageVersion

axonServerDockerImageVersion=$(prop 'axonServerDockerImageVersion')
export axonServerDockerImageVersion
# ---------- Configuring environment variables based on values from gradle.properties in the root directory. - END

# shellcheck disable=SC2086
#     - don't want quotes for variable here ("$serviceName") since they will be expanded into '' for empty variable.
docker-compose --file docker-compose-infrastructure.yml --compatibility up --detach $serviceName

docker-compose --file docker-compose-infrastructure.yml logs --follow
