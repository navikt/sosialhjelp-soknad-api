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
    mvn clean package dependency:tree help:effective-pom --batch-mode -U
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

function build_and_deploy_docker() {
    (
        cd web/target/appassembler || ( echo "Missing target directory."; exit 1; )
        docker build . -t docker.adeo.no:5000/soknadsosialhjelp-server:${versjon}
        docker push docker.adeo.no:5000/soknadsosialhjelp-server:${versjon}
    )
} 

function update_nais_settings() {
    curl -v -s -S --user "${nexusUploader}" --upload-file web/nais.yaml "https://repo.adeo.no/repository/raw/nais/soknadsosialhjelp-server/${versjon}/nais.yaml"
    curl -v -s -S --user "${nexusUploader}" --upload-file config/src/main/resources/openam/app-policies.xml "https://repo.adeo.no/repository/raw/nais/soknadsosialhjelp-server/${versjon}/am/app-policies.xml"
    curl -v -s -S --user "${nexusUploader}" --upload-file config/src/main/resources/openam/not-enforced-urls.txt "https://repo.adeo.no/repository/raw/nais/soknadsosialhjelp-server/${versjon}/am/not-enforced-urls.txt"
}

function determine_deploy() {
    nais_deploy_environment="";

    IFS=$'\n';
    for line in $(git log --first-parent --pretty=oneline -10)
    do
        IFS=' ';
        single_commit=($line)
        IFS=$'\n';
        if ! git show-ref --tags -d | grep --quiet "${single_commit[0]}"
        then
            if echo "$line" | grep '\[deploy [tq][1-9]\]'
            then
                unset IFS;
                nais_deploy_environment=$(echo "$line" | sed 's/^.*\[deploy \([tq][1-9]\)\].*$/\1/');
                return;
            fi
        else
            unset IFS;
            return;
        fi
    done

    unset IFS;
    return;
}

function deploy_if_requested_by_committer() {
    determine_deploy
    if [[ "${nais_deploy_environment}" != "" ]]
    then
        echo "Deploying version ${versjon} on ${nais_deploy_environment} with user ${domenebrukernavn}";
        curl -s -S -k -d '{"application": "soknadsosialhjelp-server","version": "'"${versjon}"'", "fasitEnvironment": "'"${nais_deploy_environment}"'", "zone": "sbs", "namespace": "'"${nais_deploy_environment}"'", "fasitUsername": "'"${domenebrukernavn}"'", "fasitPassword": "'"${domenepassord}"'", "manifesturl": "https://repo.adeo.no/repository/raw/nais/soknadsosialhjelp-server/'"${versjon}"'/nais.yaml"}' https://daemon.nais.oera-q.local/deploy || ( echo "Deploy feilet!"; exit 1; )
    fi
}

go_to_project_root
set_version
build_backend
publish
build_and_deploy_docker
update_nais_settings
deploy_if_requested_by_committer
revert_version
