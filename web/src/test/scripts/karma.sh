#!/bin/bash

echo ""
echo "Starting Karma Server (http://karma-runner.github.io)"
echo "-------------------------------------------------------------------"

karma start ../resources/jsconf/karma.conf.js $*

