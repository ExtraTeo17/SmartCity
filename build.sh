#!/bin/bash

rm -r SmartCity-build
mkdir SmartCity-build

# backend
mvn package --errors -Dmaven.test.skip=true --file backend/pom.xml 

# frontend
cd frontend
npm run build
cd ..

mv  backend/package/ ./SmartCity-build/backend
mv frontend/build/ ./SmartCity-build/frontend
