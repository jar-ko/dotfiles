#!/usr/bin/env bash

for d in $(ls -d */)
do
    echo "##### ${d}"
    cd ${d}
    eval $1
    cd ..
done
