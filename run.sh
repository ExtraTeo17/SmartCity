#!/bin/bash

set -e

buildDir="SmartCity-build";
currDir=`basename "$PWD"`

if [ "$currDir" != "$buildDir" ]; then
    if [ ! -d "$buildDir" ]; then
        ./build.sh;
    fi
    cd $buildDir
fi

serve -s frontend &
start http://localhost:5000

$JAVA_HOME/bin/java -jar backend/smartCity-2.0.jar
