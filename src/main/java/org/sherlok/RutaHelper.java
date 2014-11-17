package org.sherlok;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.regex.Pattern.compile;
import static org.sherlok.utils.CheckThat.checkArgument;
import static org.sherlok.utils.Create.list;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.cas.CAS;
import org.apache.uima.jcas.tcas.Annotation;
import org.sherlok.utils.CheckThat;

import com.google.common.base.Preconditions;

public class RutaHelper {

    final static String IDENTIFIER = "[a-zA-Z]\\w*";
    final static Pattern TYPES = compile(format(
            "DECLARE (?<superType>%s +)?(?<id>%s)(?<inlineids>(, *?%s)+)?(?<features> *?\\(.*\\))?",
            IDENTIFIER, IDENTIFIER, IDENTIFIER));

    // final static Pattern FEATURES = compile(format(
    // "(%s) +(%s)(?: *?, *?(%s) +(%s))*?", IDENTIFIER, IDENTIFIER,
    // IDENTIFIER, IDENTIFIER));

    /*-
    Syntax of declarations:
     
    Declaration -> "DECLARE" (AnnotationType)? Identifier ("," Identifier )*
                 | "DECLARE" AnnotationType Identifier ( "("FeatureDeclaration ")" )?

    FeatureDeclaration  -> ( (AnnotationType | "STRING" | "INT" | "FLOAT"
                       "DOUBLE" | "BOOLEAN") Identifier) )+
     */
    static List<TypeDTO> parseDeclaredTypes(String rutaScript) {
        List<TypeDTO> types = list();

        // Remove comments; they start with "//" and always go to end of line.
        StringBuilder cleanScriptB = new StringBuilder();
        for (String line : rutaScript.split("\n")) {
            cleanScriptB.append(line.replaceAll("//.*$", " ") + "\n");
        }
        String cleanScript = cleanScriptB.toString();

        // Normalize Whitetext and remove new lines
        cleanScript = cleanScript.replaceAll("\\r?\\n", " ");
        cleanScript = cleanScript.replaceAll(" +", " ");

        for (String instruction : cleanScript.split(";")) {
            if (instruction.trim().startsWith("DECLARE")) {

                Matcher m = TYPES.matcher(instruction.trim());
                if (m.find()) {

                    String superType = CAS.TYPE_NAME_ANNOTATION;
                    try {
                        superType = m.group("superType").trim();
                    } catch (Exception e) {// nope
                    }

                    List<TypeFeatureDTO> features = list();
                    try {
                        // remove parenthesis and trim
                        String featuresStr = m.group("features").trim()
                                .replaceAll("[\\(\\)]", "")
                                .replaceAll(" +", " ").trim();

                        for (String featureStr : featuresStr.split(",")) {
                            String[] split = featureStr.trim().split(" ");
                            checkArgument(split.length == 2,
                                    "features should come in the form of 'AnnotationType Identifier', was: "
                                            + split);
                            features.add(new TypeFeatureDTO(split[1].trim(),
                                    "", split[0].trim()));
                        }
                    } catch (Exception e) {// nope
                    }

                    String id = m.group("id");
                    types.add(new TypeDTO(id, "", superType, features));

                    try { // additional inline declarations
                        String inlineIds = m.group("inlineids");
                        for (String iid : inlineIds.substring(2).split(", ")) {
                            types.add(new TypeDTO(iid.trim(), "", superType,
                                    features));
                        }
                    } catch (Exception e) {// nope
                    }
                }
            }
        }
        return types;
    }

    public static class TypeDTO {
        public final String typeName, description, supertypeName;
        private List<TypeFeatureDTO> typeFeatures = list();

        public TypeDTO(String typeName, String description,
                String supertypeName, List<TypeFeatureDTO> features) {
            this.typeName = typeName;
            this.description = description;
            this.supertypeName = supertypeName;
            typeFeatures.addAll(features);
        }

        public List<TypeFeatureDTO> getTypeFeatures() {
            return typeFeatures;
        }

        public void addTypeFeature(TypeFeatureDTO typeFeature) {
            this.typeFeatures.add(typeFeature);
        }

        @Override
        public String toString() {
            return typeName + ":" + supertypeName;
        }
    }

    public static class TypeFeatureDTO {
        public final String featureName, description, rangeTypeName;

        public TypeFeatureDTO(String featureName, String description,
                String rangeTypeName) {
            this.featureName = featureName;
            this.description = description;
            this.rangeTypeName = rangeTypeName;
        }

        @Override
        public String toString() {
            return featureName + ":" + rangeTypeName;
        }
    }

}
