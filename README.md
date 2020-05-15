# [YAGO2geo](http://yago2geo.di.uoa.gr/)
# ABOUT
This project aims to extend the knowledge graph of [YAGO2](https://www.mpi-inf.mpg.de/departments/databases-and-information-systems/research/yago-naga/yago/) with precise geospatial and temporal information.
# MODES
* Matching (interlinking)
  * Generates Matches between entities of YAGO and the input data source (e.g., GADM).
  * Class-based matching. The input files must contain administrative units of the same administrative level (e.g., 
  geoclass_first-order_administrative division of YAGO2 and first administrative level of GADM)
* Generation
  * Generates a new RDF (.ttl) file that contains extended YAGO2 entities. In most cases, it also generates new entities
  for the unmatched entities of the input datasource.
* Topology
  * Generates topological relations between the entities of the provided RDF file.
  
