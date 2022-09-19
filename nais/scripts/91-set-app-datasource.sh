#!/usr/bin/env sh

echo "Export datasource credentials"
if [ "$NAIS_CLUSTER_NAME" == "dev-fss" ]; then
  echo "SOKNADSOSIALHJELPDATASOURCE_USERNAME already set"
else
  export SOKNADSOSIALHJELPDATASOURCE_USERNAME=$(cat /var/run/secrets/datasource/username)
fi

if [ "$NAIS_CLUSTER_NAME" == "dev-fss" ]; then
  echo "SOKNADSOSIALHJELPDATASOURCE_PASSWORD already set"
else
  export SOKNADSOSIALHJELPDATASOURCE_PASSWORD=$(cat /var/run/secrets/datasource/password)
fi
