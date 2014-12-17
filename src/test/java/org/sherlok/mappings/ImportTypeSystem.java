package org.sherlok.mappings;

import static org.sherlok.utils.Create.set;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.uima.UIMAFramework;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeDescription;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.XMLInputSource;
import org.apache.uima.util.XMLParser;
import org.sherlok.FileBased;
import org.sherlok.mappings.TypesDef.TypeDef;
import org.sherlok.utils.ValidationException;
import org.xml.sax.SAXException;

/**
 * Utility to convert a UIMA {@link TypeSystemDescription} xml into Sherlok's
 * {@link TypesDef} json.
 * 
 * @author renaud@apache.org
 */
public class ImportTypeSystem {

    /**
     * @param tsDescriptorInputFile
     *            UIMA xml typesystem file
     * @param typesDefOuputFile
     *            Sherlok json TypeDef file
     * @param prefix
     *            prefix for shortName, e.g. 'dkpro.' in 'dkpro.Entity'
     */
    @SuppressWarnings("unused")
    private static void createTypeSystemFromDescriptor(
            String tsDescriptorInputFile, String typesDefOuputFile,
            String prefix) throws InvalidXMLException, IOException,
            ResourceInitializationException, SAXException, ValidationException {

        XMLParser xmlParser = UIMAFramework.getXMLParser();

        TypeSystemDescription tsd = xmlParser
                .parseTypeSystemDescription(new XMLInputSource(
                        tsDescriptorInputFile));

        TypesDef td = new TypesDef();
        td.setName(tsd.getName());

        Set<String> names = set();

        for (TypeDescription t : tsd.getTypes()) {
            if (!names.contains(t.getName())) {
                names.add(t.getName());

                String shortName = prefix
                        + t.getName().substring(
                                t.getName().lastIndexOf('.') + 1);

                td.addType(new TypeDef().setClassz(t.getName())
                        .setDescription(t.getDescription())
                        .setShortName(shortName));
            }
        }

        FileBased.write(new File(typesDefOuputFile), td);
    }

    public static void main(String[] args) throws Exception {

        /*-
        createTypeSystemFromDescriptor(
                "/Users/richarde/dev/bluebrain/git/neuroNER/descriptor/neuroner/NeuroNERTypeSystem.xml",
                "config/types/bluima.neuroner.json", "neuroner.");
        createTypeSystemFromDescriptor(
                "/Users/richarde/.m2/repository/de/tudarmstadt/ukp/dkpro/core/de.tudarmstadt.ukp.dkpro.core.api.syntax-asl/1.6.2/de.tudarmstadt.ukp.dkpro.core.api.syntax-asl-1.6.2-sources/desc/type/Constituency.xml",
                "config/types/dkpro.parser.constituent.json", "constituent.");
        createTypeSystemFromDescriptor(
                "/Users/richarde/.m2/repository/de/tudarmstadt/ukp/dkpro/core/de.tudarmstadt.ukp.dkpro.core.api.syntax-asl/1.6.2/de.tudarmstadt.ukp.dkpro.core.api.syntax-asl-1.6.2-sources/desc/type/Chunks.xml",
                "config/types/dkpro.chunk.json", "chunk.");
        createTypeSystemFromDescriptor(
                "/Users/richarde/.m2/repository/de/tudarmstadt/ukp/dkpro/core/de.tudarmstadt.ukp.dkpro.core.api.syntax-asl/1.6.2/de.tudarmstadt.ukp.dkpro.core.api.syntax-asl-1.6.2-sources/desc/type/PennTree.xml",
                "config/types/dkpro.penntree.json", "penntree.");
         */

    }
}
