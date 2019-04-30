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
    -DgroupId=no.nav.sbl.dialogarena -Dartifaeivind@eivind-linux:~/Projects/sosialhjelp-soknad-api ((c27df1ee6d...)|BISECTING)$ git bisect good
8cbc62d70eaf31f5d17e8de6d79505ab1ae26df7 is the first bad commit
commit 8cbc62d70eaf31f5d17e8de6d79505ab1ae26df7
Author: Oskar Asplin <oskarasplin@gmail.com>
Date:   Tue Apr 9 13:11:29 2019 +0200

    DIGISOS-1049 Endret til filromat-versjon som funker utenfor image

:100644 100644 bee9f2a47e15d1ebe39bcd8ca4c22de34f38323d 029ec51e090e0f6a79eec0fe6376d142f5698405 M	pom.xmlctId=soknadsosialhjelp-filformat -Dversion=$filformat_version
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
    docker rm $(docker stop $(docker ps -a -q --filter ancestor=$image_name --format="{{.ID}}"))
    docker run -p 8080:8080 -t image_name
}

#find_filformat_version

#echo "Using filformat.version $filformat_version"

# TODO: Check if version is already available or use CLI options to toggle
#install_filformat_version

#build_project

mvn clean package -DskipTests
start_docker_image