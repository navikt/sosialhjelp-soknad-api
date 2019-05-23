#!/bin/bash
# Denne filen må ha LF som line separator.

# Stop scriptet om en kommando feiler
set -e

# Usage string
usage="Script som bygger prosjektet og deployer på Heroku

Bruk:
./$(basename "$0") OPTIONS

Gyldige OPTIONS:
    -h  | --help        - printer denne hjelpeteksten
"

# Default verdier
PROJECT_ROOT="$( cd "$(dirname "${BASH_SOURCE[0]}" )" && pwd )"
APP_NAME=""

# Hent ut argumenter
for arg in "$@"
do
case $arg in
    -h|--help)
    echo "$usage" >&2
    exit 1
    ;;
    -a=*|--app-name=*)
    APP_NAME="${arg#*=}"
    shift
    ;;
    *) # ukjent argument
    printf "Ukjent argument: %s\n" "$1" >&2
    echo ""
    echo "$usage" >&2
    exit 1
    ;;
esac
done

function get_heroku_app_name {
    # FIXME: Don't die if app name cannot be set from repo or arguments
    heroku_repo=$(git remote get-url heroku)
    APP_NAME=$(echo -n $heroku_repo | sed -E 's#^.*/(.*)\.git$#\1#')
}

function go_to_project_root {
    cd $PROJECT_ROOT
}

# FIXME: Must be inside docker build context, but this will not work on Windows
function copy_kodeverk {
    cp -r "$HOME/kodeverk/sendsoknad" .sendsoknad
}

function remove_kodeverk {
    rm -rf .sendsoknad
}

function build_project {
    mvn clean install -DskipTests
}

function heroku_login {
    heroku auth:login
    heroku container:login
}

function deploy_to_heroku {
    heroku container:push --recursive -a $APP_NAME
    heroku container:release web -a $APP_NAME
}

if [ -z "$APP_NAME" ]; then
    get_heroku_app_name
fi

echo "Build and deploy on $APP_NAME"

build_project
copy_kodeverk
deploy_to_heroku
remove_kodeverk
