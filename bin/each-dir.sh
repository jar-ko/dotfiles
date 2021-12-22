#!/usr/bin/env bash

if [ -z "${*}" ]
then
    echo "Missing command to execute"
    exit 1
fi

for d in $(ls -d */)
do
    echo "##### ${d}"
    cd "${d}"
    eval "${*}"
    cd ..
done
