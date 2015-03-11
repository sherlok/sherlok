package org.apache.uima.ruta.tag.obo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * A class to hold and process OBO ontologies, such as ChEBI. See the <a
 * href="http://oboformat.googlecode.com/svn/trunk/doc/GO.format.obo-1_2.html"
 * >OBO spec</a>.
 */
public class OBOOntology {

    public Map<String, OntologyTerm> terms = new HashMap<>();
    public Map<String, Set<String>> indexByName = new HashMap<>();

    boolean isTypeOfIsBuilt = false;

    /**
     * Read a .obo file
     * 
     * @param f
     *            The .obo file to read.
     * @throws Exception
     */
    public void read(File f) throws IOException {
        FileInputStream fis = new FileInputStream(f);
        try {
            read(new BufferedReader(new InputStreamReader(fis, "UTF-8")));
        } finally {
            try {
                fis.close();
            } catch (Exception e) {

            }
        }
    }

    void read(BufferedReader br) throws IOException {
        List<String> lines = new ArrayList<String>();
        boolean inTerm = false;
        String line = br.readLine();
        while (line != null) {
            if (line.matches("\\[.*\\]")) {
                if (inTerm) {
                    handleTerm(lines);
                    inTerm = false;
                    lines = new ArrayList<String>();
                }
                if (line.equals("[Term]"))
                    inTerm = true;
            } else {
                if (inTerm)
                    lines.add(line);
            }
            line = br.readLine();
        }
    }

    private void handleTerm(List<String> lines) {
        OntologyTerm term = new OntologyTerm(lines);
        terms.put(term.getId(), term);
        indexTerm(term);
    }

    /**
     * Add a single OntologyTerm to the ontology.
     * 
     * @param term
     *            The OntologyTerm.
     */
    public void addTerm(OntologyTerm term) {
        terms.put(term.getId(), term);
        indexTerm(term);
        isTypeOfIsBuilt = false;
    }

    /**
     * Merge a whole ontology into the current one.
     * 
     * @param ont
     *            The ontology to merge in.
     */
    public void addOntology(OBOOntology ont) {
        for (String id : ont.terms.keySet()) {
            addTerm(ont.terms.get(id));
        }
    }

    private void indexTerm(OntologyTerm term) {
        Set<String> names = new HashSet<String>();
        names.add(term.getName());
        for (Synonym s : term.getSynonyms()) {
            String st = s.getType();
            if (st != null && !st.matches(".*(InChI|SMILES|FORMULA).*")) {
                names.add(st);
            }
        }
        for (String termName : names) {
            Set<String> termIds = indexByName.get(termName);
            if (termIds == null) {
                termIds = new HashSet<String>();
                indexByName.put(termName, termIds);
            }
            termIds.add(term.getId());
        }
    }

    private void buildIsTypeOf() {
        if (isTypeOfIsBuilt)
            return;
        for (String termId : terms.keySet()) {
            OntologyTerm term = terms.get(termId);
            for (String isA : term.getIsA()) {
                terms.get(isA).addIsTypeOf(termId);
            }
        }
        isTypeOfIsBuilt = true;
    }

    /**
     * Writes a file suitable for use as onotology.txt.
     * 
     * @param pw
     *            The PrintWriter to write to.
     */
    public void writeOntTxt(PrintWriter pw) {
        for (String id : terms.keySet()) {
            OntologyTerm term = terms.get(id);
            Set<String> synSet = new HashSet<String>();
            synSet.add(term.getName());
            for (Synonym s : term.getSynonyms()) {
                String st = s.getType();
                if (id.startsWith("PTCO")
                        || (st != null && !st
                                .matches(".*(InChI|SMILES|FORMULA).*"))) {
                    String synonym = s.getSyn();
                    if (!synonym.matches("\\S")) {// reject ontology terms
                                                  // consisting of a single
                                                  // letter as these produce
                                                  // too
                                                  // many false positives
                        synSet.add(synonym);
                    }
                }
            }

            pw.println("[" + id + "]");
            for (String syn : synSet)
                pw.println(syn);
            pw.println();
        }
        pw.flush();
    }

    /**
     * Look up a term by name (or synonym), and return the IDs.
     * 
     * @param s
     *            The term name to look up.
     * @return The IDs for the name, or null.
     */
    public Set<String> getIdsForTerm(String s) {
        return indexByName.get(s);
    }

    /**
     * Given a set of IDs, return a set that contains all of the IDs, the
     * parents of those IDs, the grandparents, etc.
     * 
     * @param termIds
     *            The initial "seed" IDs.
     * @return The full set of IDs.
     */
    public Set<String> getIdsForIdsWithAncestors(Collection<String> termIds) {
        Stack<String> idsToConsider = new Stack<String>();
        idsToConsider.addAll(termIds);
        Set<String> resultIds = new HashSet<String>();
        while (!idsToConsider.isEmpty()) {
            String id = idsToConsider.pop();
            if (!resultIds.contains(id)) {
                resultIds.add(id);
                if (terms.containsKey(id))
                    idsToConsider.addAll(terms.get(id).getIsA());
            }
        }
        return resultIds;
    }

    /**
     * Given a single ID, return that ID, its parents, grandparents etc.
     * 
     * @param termId
     *            The initial "seed" ID.
     * @return The full set of IDs.
     */
    public Set<String> getIdsForIdWithAncestors(String termId) {
        if (!terms.containsKey(termId))
            return new HashSet<String>();
        Stack<String> idsToConsider = new Stack<String>();
        idsToConsider.add(termId);
        Set<String> resultIds = new HashSet<String>();
        while (!idsToConsider.isEmpty()) {
            String id = idsToConsider.pop();
            if (!resultIds.contains(id)) {
                resultIds.add(id);
                idsToConsider.addAll(terms.get(id).getIsA());
            }
        }
        return resultIds;
    }

    /**
     * Look up a term by name, and return its ID and the IDs of all of its
     * ancestors.
     * 
     * @param s
     *            The term name to look up.
     * @return The full set of IDs, empty if the term was not found.
     */
    public Set<String> getIdsForTermWithAncestors(String s) {
        if (!indexByName.containsKey(s))
            return new HashSet<String>();
        Stack<String> idsToConsider = new Stack<String>();
        idsToConsider.addAll(getIdsForTerm(s));
        Set<String> resultIds = new HashSet<String>();
        while (!idsToConsider.isEmpty()) {
            String id = idsToConsider.pop();
            if (!resultIds.contains(id)) {
                resultIds.add(id);
                idsToConsider.addAll(terms.get(id).getIsA());
            }
        }
        return resultIds;
    }

    /**
     * Given a set of IDs, return a set that contains all of the IDs, the
     * children of those IDs, the grandchildren, etc.
     * 
     * @param s
     *            The initial "seed" ID.
     * @return The full set of IDs.
     */
    public Set<String> getIdsForIdWithDescendants(String s) {
        buildIsTypeOf();
        Stack<String> idsToConsider = new Stack<String>();
        idsToConsider.add(s);
        Set<String> resultIds = new HashSet<String>();
        while (!idsToConsider.isEmpty()) {
            String id = idsToConsider.pop();
            if (!resultIds.contains(id)) {
                resultIds.add(id);
                if (terms.containsKey(id))
                    idsToConsider.addAll(terms.get(id).getIsTypeOf());
            }
        }
        return resultIds;
    }

    /**
     * Look up a term by name, and return its ID and the IDs of all of its
     * descendants.
     * 
     * @param s
     *            The term name to look up.
     * @return The full set of IDs, empty if the term was not found.
     */
    public Set<String> getIdsForTermWithDescendants(String s) {
        buildIsTypeOf();
        if (!indexByName.containsKey(s))
            return new HashSet<String>();
        Stack<String> idsToConsider = new Stack<String>();
        idsToConsider.addAll(getIdsForTerm(s));
        Set<String> resultIds = new HashSet<String>();
        while (!idsToConsider.isEmpty()) {
            String id = idsToConsider.pop();
            if (!resultIds.contains(id)) {
                resultIds.add(id);
                idsToConsider.addAll(terms.get(id).getIsTypeOf());
            }
        }
        return resultIds;
    }

    /**
     * Given a set of seed IDs, expand that set of IDs to include all ancestor
     * IDs, then return a map from each ID in the set to the descendant IDs
     * (including the ID itself).
     * 
     * @param ids
     *            The seed IDs.
     * @return The mapping.
     */
    public Map<String, Set<String>> queriesForIds(Collection<String> ids) {
        Set<String> idsWithParents = getIdsForIdsWithAncestors(ids);
        Map<String, Set<String>> queries = new HashMap<String, Set<String>>();
        for (String id : idsWithParents) {
            queries.put(id, getIdsForIdWithDescendants(id));
        }
        return queries;
    }

    /**
     * Tests whether there is a direct is_a (or has_role) relationship between
     * two IDs.
     * 
     * @param hypoID
     *            The potential hyponym (child term).
     * @param hyperID
     *            The potential hypernym (parent term).
     * @return Whether that direct relationship exists.
     */
    public boolean directIsA(String hypoID, String hyperID) {
        if (!terms.containsKey(hypoID))
            return false;
        OntologyTerm term = terms.get(hypoID);
        if (term.getIsA().contains(hyperID))
            return true;
        return false;
    }

    /**
     * Tests whether there is a direct or indirect is_a (or has_role)
     * relationship between two IDs.
     * 
     * @param hypoID
     *            The potential hyponym (descendant term).
     * @param hyperID
     *            The potential hypernym (ancestor term).
     * @return Whether that direct relationship exists.
     */
    public boolean isA(String hypoID, String hyperID) {
        if (hypoID.equals(hyperID))
            return false;
        return getIdsForIdWithAncestors(hypoID).contains(hyperID);
    }

    /**
     * Looks up the name for an ontology ID.
     * 
     * @param id
     *            The ontology ID.
     * @return The name, or null.
     */
    public String getNameForID(String id) {
        if (!terms.containsKey(id))
            return null;
        return terms.get(id).getName();
    }

    /**
     * Looks up the definition for an ontology ID.
     * 
     * @param id
     *            The ontology ID.
     * @return The definition, or null.
     */
    public String getDefinitionForID(String id) {
        if (!terms.containsKey(id))
            return null;
        return terms.get(id).getDef();
    }

    /**
     * Outputs the term dictionary.
     * 
     * @return The term dictionary.
     */
    public Map<String, OntologyTerm> getTerms() {
        return terms;
    }

}
