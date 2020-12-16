#!/bin/bash

buildDir="SmartCity-build";

rm -rf $buildDir
mkdir $buildDir

# backend
mvn package --errors -Dmaven.test.skip=true --file backend/pom.xml 

# frontend
cd frontend
npm run build
cd ..

mv  backend/package/ ./$buildDir/backend
mv frontend/build/ ./$buildDir/frontend
cp run.sh $buildDir/
