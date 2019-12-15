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
    mvn clean deploy --batch-mode -U
}

function build_and_deploy_docker() {
    (
        docker build . -t docker.adeo.no:5000/sosialhjelp-soknad-api:${versjon}
        docker push docker.adeo.no:5000/sosialhjelp-soknad-api:${versjon}
    )
}

function update_nais_settings() {
    curl -v -s -S --user "${nexusUploader}" --upload-file web/nais.yaml "https://repo.adeo.no/repository/raw/nais/sosialhjelp-soknad-api/${versjon}/nais.yaml"
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

# Quick-and-Dirty: Bytt til en standardisert løsning.
function deploy() {
    application="$1"
    environment="$2"
    releaseVersion="$3"
    namespace="${environment}"
    zone="sbs"

    # Uses: JIRA_USERNAME, JIRA_PASSWORD

    # Generert:
    declare -A environmentIds
    environmentIds[d1]=18650
    environmentIds[d2]=18651
    environmentIds[d3]=18652
    environmentIds[d4]=18653
    environmentIds[d230]=18656
    environmentIds[d231]=18657
    environmentIds[d234]=18658
    environmentIds[dx]=18654
    environmentIds[dy]=18655
    environmentIds[dz]=21751
    environmentIds[dv]=21752
    environmentIds[o1]=19150
    environmentIds[p]=17658
    environmentIds[q0]=16824
    environmentIds[q1]=16825
    environmentIds[q2]=16652
    environmentIds[q3]=16653
    environmentIds[q4]=16647
    environmentIds[q5]=16649
    environmentIds[q6]=16648
    environmentIds[q7]=16794
    environmentIds[q8]=16651
    environmentIds[q9]=16654
    environmentIds[q10]=18673
    environmentIds[q11]=20250
    environmentIds[q230]=16771
    environmentIds[q231]=16772
    environmentIds[q234]=16773
    environmentIds[q411]=16774
    environmentIds[q412]=16775
    environmentIds[q415]=16814
    environmentIds[q469]=16776
    environmentIds[qa]=16768
    environmentIds[qb]=16769
    environmentIds[qc]=16770
    environmentIds[qx]=16815
    environmentIds[t0]=16556
    environmentIds[t1]=16557
    environmentIds[t2]=16558
    environmentIds[t3]=16559
    environmentIds[t4]=16560
    environmentIds[t5]=16561
    environmentIds[t6]=16562
    environmentIds[t7]=16563
    environmentIds[t8]=16564
    environmentIds[t9]=16565
    environmentIds[t10]=16566
    environmentIds[t11]=16567
    environmentIds[t12]=16763
    environmentIds[t13]=21750
    environmentIds[t411]=16765
    environmentIds[t412]=16766
    environmentIds[t415]=16813
    environmentIds[t469]=16767
    environmentIds[ta]=16764
    environmentIds[tpr-u1]=16667
    environmentIds[tx]=17550
    environmentIds[ty]=17551
    environmentIds[u1]=16657
    environmentIds[u2]=16658
    environmentIds[u3]=16659
    environmentIds[u4]=16660
    environmentIds[u5]=16661
    environmentIds[u6]=16662
    environmentIds[u7]=16663
    environmentIds[u8]=16664
    environmentIds[u9]=16665
    environmentIds[u10]=16666
    environmentIds[u11]=19151
    environmentIds[u12]=16795
    environmentIds[u13]=16796
    environmentIds[u14]=16797
    environmentIds[u15]=16798
    environmentIds[u16]=16799
    environmentIds[u17]=19152
    environmentIds[u18]=16800
    environmentIds[u19]=16801
    environmentIds[u21]=22152
    environmentIds[u22]=16802
    environmentIds[u23]=18008
    environmentIds[ussi1]=22579
    environmentIds[ci]=23369

    declare -A zoneIds
    zoneIds[fss]=23451
    zoneIds[sbs]=23452

    environmentId=${environmentIds[${environment}]}
    zoneId=${zoneIds[${zone}]}

    read -d '' postBodyString << EOF || true
        {
            "fields": {
                "project": {
                    "key": "DEPLOY"
                },
                "issuetype": {
                    "id": "14302"
                },
                "customfield_14811": {
                    "id": "${environmentId}",
                    "value": "${environmentId}"
                },
                "customfield_14812": "${application}:${releaseVersion}",
                "customfield_19413": "${namespace}",
                "customfield_19610": {
                    "id": "${zoneId}",
                    "value": "${zoneId}"
                },
                "summary": "Automatisk deploy"
            }
        }
EOF

    echo "Deploying version ${releaseVersion} on ${namespace} with user ${JIRA_USERNAME}";
    deploy_result=$(curl --output /dev/stderr --write-out "%{http_code}" --user "${JIRA_USERNAME}:${JIRA_PASSWORD}" -X POST --header "Content-Type: application/json" -d "${postBodyString}" "https://jira.adeo.no/rest/api/2/issue/")
    if [[ "${deploy_result}" != "201" ]]
    then
        echo "Deployment failed!";
        exit 1;
    else
        echo "Deployment initiated.";
    fi
}

function deploy_if_requested_by_committer() {
    determine_deploy
    if [[ "${nais_deploy_environment}" != "" ]]
    then
        deploy "sosialhjelp-soknad-api" "${nais_deploy_environment}" "${versjon}"
    fi
}

go_to_project_root
set_version
publish
build_and_deploy_docker
update_nais_settings
deploy_if_requested_by_committer
revert_version
