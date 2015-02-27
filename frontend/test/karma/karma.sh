#!/bin/bash

echo ""
echo "Starting Karma Server (http://karma-runner.github.io)"
echo "-------------------------------------------------------------------"

karma start karma.conf.js $*
