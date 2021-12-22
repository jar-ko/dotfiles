#!/usr/bin/env bash

if [ -z "${1}" ]
then
    echo "Missing parameter"
    exit 1
fi

for d in $(ls -d */)
do
    echo "##### ${d}"
    cd "${d}"
    eval "${1}"
    cd ..
done
