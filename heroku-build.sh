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

# Hent ut argumenter
for arg in "$@"
do
case $arg in
    -h|--help)
    echo "$usage" >&2
    exit 1
    ;;
    -n|--namespace)
    echo "set namespace"
    ;;
    -e|--external)
    echo "build external..."
    ;;
    -s|--skip-dependencies)
    echo "don't build dependencies"
    ;;
    *) # ukjent argument
    printf "Ukjent argument: %s\n" "$1" >&2
    echo ""
    echo "$usage" >&2
    exit 1
    ;;
esac
done

function go_to_project_root() {
    cd $PROJECT_ROOT
}

function find_filformat_version() {
    filformat_version=$(perl -ne 'print"$1" if /filformat.version>([^<]+)/' pom.xml)
}

function install_filformat_version() {
    git clone https://github.com/navikt/soknadsosialhjelp-filformat.git $filformat_version
    cd $filformat_version
    git checkout tags/$filformat_version
    mvn clean install
    mvn install:install-file -Dfile=target/soknadsosialhjelp-filformat-1.0-SNAPSHOT.jar \
    -DgroupId=no.nav.sbl.dialogarena -DartifactId=soknadsosialhjelp-filformat -Dversion=$filformat_version
    go_to_project_root
    rm -rf $filformat_version
}

function build_project() {
    mvn clean install -DskipTests
}

function start_docker_image() {
    # FIXME: Deploy fat jar to Heroku, but useful for testing mocked environment locally
    image_name=backend-test
    docker build -t $image_name -f DockerfileHeroku .
    docker run -p 8080:8080 -t $image_name
}

function stop_docker_images() {
    for container_id in $(docker ps --filter name=$image_name --format="{{.ID}}")
    do
        echo "Stopper container $container_id"
        docker rm -f $container_id
    done
}

function heroku_login {
    heroku auth:login
    heroku container:login
}

function create_app {
    app_name=$1
    # Name must
    # - not be taken
    # - not be over 30 characters
    # - start with a letter, end with a letter or digit and can only contain lowercase letters, digits, and dashes
    heroku create $app_name
}

# FIXME: App name (-a/--app) must be provided to all Heroku commands if app was not created with Heroku CLI
function deploy_app {
    heroku container:push --recursive
    heroku container:release web
}

function restart_app {
    heroku ps:restart
}

function shell_to_app {
    heroku run bash
}

# TODO: Check if version is already available or use CLI options to toggle installation
#find_filformat_version
#echo "Using filformat.version $filformat_version"
#install_filformat_version

build_project

# TODO: Add "dry-run" flag to run as container on localhost
#stop_docker_images
#start_docker_image

#heroku_login
#App can be created manually
#create_app test-asdf-server

deploy_app
