#!/bin/sh
set -e

buildDir="SmartCity-build";
jarFile="backend/smartCity-2.0.jar"

if [ ! -f "$jarFile" ] || [ ! -f "frontend/index.html" ]; then
    if [ ! -d "$buildDir" ]; then
        if [ -d "scripts" ]; then
            cd scripts
            ./build.sh;
            cd ..
        else 
            echo Missing files required to run app
            exit 1;
        fi
    fi
    cd $buildDir
fi

serve -s -n frontend &
if command -v start &> /dev/null; then
    start http://localhost:5000
else
    echo You need to start localhost:5000 by yourself
fi


"$JAVA_HOME/bin/java" -jar "$jarFile"
