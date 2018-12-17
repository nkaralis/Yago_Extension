#!/bin/bash
# extract data from YAGO for a specific class
# YAGO_TYPES="/home/nkaralis/yago2_extension/extended_official/GRC/version3/yago/GRC_yago_allEntities_Types.nt"
YAGO_TYPES="/home/nkaralis/yago2_extension/yago2018/yagoGeonamesTypes.tsv"
YAGO_GEONAMES_DATA="/home/nkaralis/yago2_extension/yago2018/yagoGeonamesOnlyData.tsv"
YAGO_LABELS="/home/nkaralis/yago2_extension/yago2018/yagoLiteralFacts.tsv"
YAGO_LITERAL_FACTS="/home/nkaralis/yago2_extension/yago2018/yagoLabels.tsv"
# YAGO_GEOM="/home/nkaralis/yago2_extension/yago/geom/yagoGeonamesPointsWKT.nt"
TYPE=$1
CLASS="<$TYPE/prehistoric_site>"
output_types=$TYPE"_types.tsv"
output_data=$TYPE"_data.tsv"

types="$(awk --assign type="$CLASS" '$3==type {print}' $YAGO_TYPES)"
echo "${types}">$output_types

entities="$(echo "${types}" | awk '{print $1}')"
echo "${entities}" > temp
cat $YAGO_GEONAMES_DATA $YAGO_LABELS $YAGO_LITERAL_FACTS > temp3
grep -Ff temp temp3 > temp2
grep -vF "isLocatedIn" temp2 > temp4
cut -f1 --complement temp4 > $output_data
# grep -F "rdfs:label" $output_data > $output_names
# grep -Ff temp $YAGO_GEOM > $output_geom
rm temp temp2 temp3 temp4
