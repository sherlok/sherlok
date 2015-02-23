package org.sherlok.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;

import ch.epfl.bbp.uima.obo.OBOOntology;
import ch.epfl.bbp.uima.obo.OntologyTerm;
import ch.epfl.bbp.uima.obo.Synonym;

/**
 * Coverts three HBP OBO ontologies into Ruta WORDTABLES to be consumed by
 * NeuroNER.
 * 
 * @author richarde
 */
public class Obo2Wordtable {

    private static final String OBO_INPUT = "/Volumes/HDD2/ren_data/dev_hdd/bluebrain/git2/ontologies/the_ontologies/";
    private static final String WORDTABLE_OUTPUT = "/Users/richarde/dev/bluebrain/git/neuroNER/resources/bluima/neuroner";

    @SuppressWarnings("resource")
    public static void main(String[] args) throws IOException {

        for (String oboFile : new String[] { "hbp_developmental_ontology",
                "hbp_layer_ontology", "hbp_neurotransmitter_ontology" }) {

            OBOOntology obo = new OBOOntology();
            obo.read(new File(OBO_INPUT, oboFile + ".obo"));

            FileWriter wordtableWriter = new FileWriter(new File(
                    WORDTABLE_OUTPUT, oboFile + ".csv"));

            // TextFileWriter wordtableWriter = new TextFileWriter(new File(
            // WORDTABLE_OUTPUT, oboFile + ".csv"));
            // TODO can add comment at top with 'generated by ...'?

            Set<String> uniqueIds = Create.set();
            for (Entry<String, OntologyTerm> termE : obo.terms.entrySet()) {

                OntologyTerm term = termE.getValue();
                String id = term.getId();
                if (uniqueIds.contains(id)) {
                    throw new RuntimeException("duplicate id: " + id);
                }
                uniqueIds.add(id);

                String name = term.getName();
                wordtableWriter.write(name + ";" + id + "\n");

                for (Synonym syn : term.getSynonyms()) {
                    wordtableWriter.write(syn.getSyn() + ";" + id + "\n");
                }
            }

            wordtableWriter.close();
        }
    }
}
