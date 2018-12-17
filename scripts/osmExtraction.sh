#!/bin/bash
# transform shapefiles of Geofabrik to RDF
for file in $1/*zip; do
	echo $file
	unzip -d temp_dir $file
	python /home/nkaralis/yago2_extension/scripts/shp2nt_osm.py ./temp_dir/gis_osm_pois_a_free_1.shp $2/osm_pois_a_free_1.nt
	python /home/nkaralis/yago2_extension/scripts/shp2nt_osm.py ./temp_dir/gis_osm_landuse_a_free_1.shp $2/osm_landuse_a_free_1.nt
	python /home/nkaralis/yago2_extension/scripts/shp2nt_osm.py ./temp_dir/gis_osm_natural_a_free_1.shp $2/osm_natural_a_free_1.nt
	python /home/nkaralis/yago2_extension/scripts/shp2nt_osm.py ./temp_dir/gis_osm_places_a_free_1.shp $2/osm_places_a_free_1.nt
	python /home/nkaralis/yago2_extension/scripts/shp2nt_osm.py ./temp_dir/gis_osm_water_a_free_1.shp $2/osm_water_a_free_1.nt
	python /home/nkaralis/yago2_extension/scripts/shp2nt_osm.py ./temp_dir/gis_osm_waterways_free_1.shp $2/osm_waterways_free_1.nt
	rm -r temp_dir
done 