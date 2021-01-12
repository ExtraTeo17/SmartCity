#!/bin/bash

if [ "$1" != "" ]; then
  runs=$1;
else
  runs=10
fi
echo "Runs = $runs";

cd ..; 

echo "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%"
echo "-----------------------------------------------------------------------------------"
echo "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%"
i=0;
while (( i < $runs )); do
    ./run.sh &
    pid=$!
    sleep 15; 
    node scripts/connect.js
    wait $pid
    ((i++))
done

cd scripts;
