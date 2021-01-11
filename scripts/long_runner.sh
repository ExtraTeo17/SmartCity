#!/bin/bash

for i in {1..10}
do
    cd ..; 
    ./run.sh &
    pid=$!
    sleep 15; 
    node scripts/connect.js
    wait $pid
done

