#!/bin/bash
# get data from OSM files for a specific class
OSM_DATA=$1
OSM_FCLASS=$2

output_class=$OSM_FCLASS"_data.nt"

grep -F "hasOSM_FClass" $OSM_DATA > temp1
entities="$(awk --assign type=$OSM_FCLASS -F "\"| " '$4==type {print $1}' temp1)"
echo "${entities}" > temp
grep -Ff temp $OSM_DATA | sort -u > $output_class
rm temp temp1