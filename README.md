# [YAGO2geo](http://yago2geo.di.uoa.gr/)
# ABOUT
This project aims to extend the knowledge graph of [YAGO](https://www.mpi-inf.mpg.de/departments/databases-and-information-systems/research/yago-naga/yago/) with more geospatial and temporal information.
# MODES
* Matching
  * Generates Matches between entities of YAGO and the input data source (e.g., GADM).
  * Semi-automatic process. The user needs to provide the data that they want to match.
  * Example: yago_extension matching --yago=geoclass_first-order_administrative_division.ttl --datasource=gadm_admLevel1.nt --output=1level_matches.ttl --threads=4
* Generation
  * Generates two N-TRIPLES files:
    * matched: Contains the matched entities of YAGO with the information provided by the data source.
    * unmatched: Contains new entities that are created by the unmatched entities of the data source.
  * Example: yago_extension generation --matches=1level_matches.ttl --data=gadm_admLevel1.nt --matched=yago_gadm_matched1.nt --unmatched=yago_gadm_unmatched1.nt --origin=gadm
* Topology
  * Generates topological relations between the entities of the provided RDF (N-TRIPLES, TURTLE) file.
  * Example: yago_extension topology --kg=yago_gadm_matched1.nt --output=yago_gadm_matched1_topo-rel.nt --threads=4
  
