#!/bin/bash

HOSTNAME=$(hostname)
BASEURL="http://$HOSTNAME.devillo.no:8181/sendsoknad/soknad/Dagpenger"

echo ""
echo "Starting Protractor"
echo "Running tests against $BASEURL"
echo "-------------------------------------------------------------------"

protractor ../resources/jsconf/protractor.conf.js --baseUrl=$BASEURL
