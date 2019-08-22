#!/usr/bin/env sh

export SOKNADSOSIALHJELPDATASOURCE_USERNAME=$(cat /var/run/secrets/datasource/username)
export SOKNADSOSIALHJELPDATASOURCE_PASSWORD=$(cat /var/run/secrets/datasource/password)
