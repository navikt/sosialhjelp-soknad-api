#!/bin/bash

HOSTNAME=$(hostname)
BASEURL="http://$HOSTNAME.devillo.no:8181/sendsoknad/soknad"

echo ""
echo "Starting Protractor"
echo "Running tests against $BASEURL"
echo "-------------------------------------------------------------------"

protractor protractor.conf.js --baseUrl=$BASEURL

