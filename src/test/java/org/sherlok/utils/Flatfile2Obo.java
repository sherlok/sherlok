package org.sherlok.utils;

import static java.nio.charset.Charset.defaultCharset;
import static java.nio.file.Files.readAllLines;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import org.apache.uima.ruta.tag.obo.OBOOntology;
import org.apache.uima.ruta.tag.obo.OboFormatException;
import org.apache.uima.ruta.tag.obo.OntologyTerm;
import org.apache.uima.ruta.tag.obo.Synonym;

public class Flatfile2Obo {

    public static void main(String[] args) throws IOException,
            OboFormatException {

        String root = "/Volumes/HDD2/ren_data/dev_hdd/bluebrain/git2/neuroNER/resources/bluima/neuroner/";

        Set<String> existingTerms = new HashSet<>();
        OBOOntology existingObo = new OBOOntology().read(new File(root,
                "hbp_brainregions_aba-syn.obo"));
        for (OntologyTerm t : existingObo.getTerms().values()) {
            existingTerms.add(t.getName());
            for (Synonym s : t.getSynonyms()) {
                existingTerms.add(s.getSyn());
            }
        }

        PrintWriter writer = new PrintWriter(new File(root + "regions.obo"));

        int id = 1;

        for (String f : new String[] { "regions_adverbs_al.txt",
                "regions_adverbs_ic.txt", "regions_lfrench.txt", "regions.txt" }) {

            for (String name : readAllLines(new File(root, f).toPath(),
                    defaultCharset())) {
                if (!existingTerms.contains(name)) {
                    writer.println("\n[Term]\nid: UNKN_REGION:" + id++
                            + "\nname: " + name);
                }
            }
        }
        writer.close();
    }
}
