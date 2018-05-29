#!/bin/bash
# Denne filen må ha LF som line separator.

# Stop scriptet om en kommando feiler
set -e

# Usage string
usage="Script som bygger prosjektet og publiserer til nexus

Om environment variabelen 'versjon' er satt vil den erstatte versjonen som ligger i pom.xml.

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

function build_backend() {
    mvn clean verify dependency:tree help:effective-pom --batch-mode -U
}

function set_version() {
    if [[ ${versjon+x} ]]; then
        mvn versions:set -U -DnewVersion=${versjon}
    fi
}

function revert_version() {
    if [[ ${versjon+x} ]]; then
        mvn versions:revert
    fi
}

function publish() {
    mvn deploy --batch-mode -DskipTests
}

go_to_project_root
set_version
build_backend
publish
revert_version