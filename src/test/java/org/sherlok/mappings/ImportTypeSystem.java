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
 * Imports UIMA {@link TypeSystemDescription} into Sherlok's {@link TypesDef}
 * 
 * @author richarde
 */
public class ImportTypeSystem {

    private static void createTypeSystemFromDescriptor(String tsDescriptorPath,
            String typesDefPath, String prefix) throws InvalidXMLException,
            IOException, ResourceInitializationException, SAXException,
            ValidationException {

        XMLParser xmlParser = UIMAFramework.getXMLParser();

        TypeSystemDescription tsd = xmlParser
                .parseTypeSystemDescription(new XMLInputSource(tsDescriptorPath));

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

        FileBased.write(new File(typesDefPath), td);
    }

    public static void main(String[] args) throws Exception {

        createTypeSystemFromDescriptor(
                "/Users/richarde/dev/bluebrain/git/neuroNER/descriptor/neuroner/NeuroNERTypeSystem.xml",
                "config/types/bluima.neuroner.json", "neuroner.");

    }
}
