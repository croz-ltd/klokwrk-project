#!/bin/bash

# ---------- Configuring environment variables based on values from gradle.properties in the root directory. - START
function prop {
  grep "${1}" ../../gradle.properties | cut -d '=' -f2 | sed 's/ //g'
}

postgreSqlDockerImageVersion=$(prop 'postgreSqlDockerImageVersion')
export postgreSqlDockerImageVersion

axonServerDockerImageVersion=$(prop 'axonServerDockerImageVersion')
export axonServerDockerImageVersion
# ---------- Configuring environment variables based on values from gradle.properties in the root directory. - END

docker-compose --file docker-compose-infrastructure.yml down --volumes
