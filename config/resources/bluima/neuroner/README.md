
* ALL LEXICA IN HERE ARE COMPILED TO `generated.mtwl`
* YOU NEED TO RECOMPILE THEM EVERY TIME YOU ADD A TERM TO ONE OF THE `.txt` FILES
    * (select all files, right click and `UIMA Ruta` -> `convert to multi TWL`)

### Manually added lexica:

* developmental.txt
* functions.txt
* morphology.txt
* neuron_triggers.txt
* neurotransmitter.txt
* proteins_ns_dan.txt
	* based on a list from Daniel.Keller@epfl.ch
* regions.txt
* regions_adverbs_ic.txt
    * manually filtered list from Missing on 150k abstracts ending with 'ic' 
* regions_adverbs_al.txt
    * manually filtered list from Missing on 150k abstracts ending with 'al' 

### Imported Lexica

* proteins_obo_pro.txt
	* names from OBO PRO ontology
* regions_lfrench.txt
	* compiled from Leon French resources
* species.txt and species.csv
    * top 1000 most frequent species found with Linnaeus in PubMed abstracts
	* `select species, species_id, count(species) from species group by species order by count(species) desc`