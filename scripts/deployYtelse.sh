#!/bin/bash
mvn jboss:deploy -Dusername=deployer -Dpassword=imusedtodeploystuff -Pu5 -f config/pom.xml 
