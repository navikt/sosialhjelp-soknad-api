#!/usr/bin/env bash
# Denne filen må ha LF som line separator.

# Bruk: "./heroku-build.sh feature-navn-api"

# Default verdier
APP_NAME=$1

if [[ -z "$APP_NAME" ]]; then
    # FIXME: Don't die if app name cannot be set from repo or arguments
    heroku_repo=$(git remote get-url heroku)
    APP_NAME=$(echo -n ${heroku_repo} | sed -E 's#^.*/(.*)\.git$#\1#')
fi

mvn clean install -DskipTests
heroku container:push --recursive -a ${APP_NAME}
heroku container:release web -a ${APP_NAME}
