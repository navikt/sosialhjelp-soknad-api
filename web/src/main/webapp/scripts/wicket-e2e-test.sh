#!/bin/bash

BASE_DIR=`dirname $0`

echo ""
echo "Starting Karma e2e test"
echo "-------------------------------------------------------------------"

karma start $BASE_DIR/../test/config/karma-e2e-wicket.conf.js $*
