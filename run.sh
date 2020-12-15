#!/bin/bash

buildDir="SmartCity-build";

if [ ! -d "$buildDir" ]; then
    ./build.sh;
fi

cd $buildDir

serve -s frontend &
start http://localhost:5000

java -jar backend/smartCity-2.0.jar

