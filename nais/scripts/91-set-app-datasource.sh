#!/usr/bin/env sh

echo "Export datasource credentials"
export SOKNADSOSIALHJELPDATASOURCE_USERNAME=$(cat /var/run/secrets/datasource/username)
export SOKNADSOSIALHJELPDATASOURCE_PASSWORD=$(cat /var/run/secrets/datasource/password)
