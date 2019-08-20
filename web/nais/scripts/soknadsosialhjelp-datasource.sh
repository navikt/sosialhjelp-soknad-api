#!/usr/bin/env bash
export SOKNADSOSIALHJELPDATASOURCE_URL=$(cat /var/run/secrets/datasource/url)
export SOKNADSOSIALHJELPDATASOURCE_USERNAME=$(cat /var/run/secrets/datasource/username)
export SOKNADSOSIALHJELPDATASOURCE_PASSWORD=$(cat /var/run/secrets/datasource/password)
