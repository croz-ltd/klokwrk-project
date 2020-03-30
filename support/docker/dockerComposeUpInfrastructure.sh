#!/bin/bash

serviceName=$1

# shellcheck disable=SC2086
#     - don't want quotes for variable here ("$serviceName") since they will be expanded into '' for empty variable.
docker-compose --file docker-compose-infrastructure.yml --compatibility up --detach $serviceName

docker-compose --file docker-compose-infrastructure.yml logs --follow
