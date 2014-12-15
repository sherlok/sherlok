/**
 * Copyright (C) 2014 Renaud Richardet (renaud@apache.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sherlok;

import static java.lang.String.format;
import static java.util.regex.Pattern.compile;
import static org.sherlok.utils.CheckThat.validateArgument;
import static org.sherlok.utils.Create.list;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.cas.CAS;
import org.sherlok.utils.ValidationException;

/**
 * Helper to parse Ruta script type declarations.
 * 
 * @author renaud@apache.org
 */
public class RutaHelper {

    final static String IDENTIFIER = "[a-zA-Z]\\w*";
    final static Pattern TYPES = compile(format(
            "DECLARE (?<superType>%s +)?(?<id>%s)(?<inlineids>(, *?%s)+)?(?<features> *?\\(.*\\))?",
            IDENTIFIER, IDENTIFIER, IDENTIFIER));

    /*-
    Syntax of declarations:
     
    Declaration -> "DECLARE" (AnnotationType)? Identifier ("," Identifier )*
                 | "DECLARE" AnnotationType Identifier ( "("FeatureDeclaration ")" )?

    FeatureDeclaration  -> ( (AnnotationType | "STRING" | "INT" | "FLOAT"
                       "DOUBLE" | "BOOLEAN") Identifier) )+
     */
    /**
     * @param rutaScript
     *            a Ruta script to parse
     * @return a set of annotation types
     */
    public static Set<TypeDTO> parseDeclaredTypes(String rutaScript)
            throws ValidationException {
        // LHS since insertion order is important & avoids duplicate TypeDTOs
        LinkedHashSet<TypeDTO> types = new LinkedHashSet<TypeDTO>();

        // Remove comments; they start with "//" and always go to end of line.
        StringBuilder sb = new StringBuilder();
        for (String line : rutaScript.split("\n")) {
            sb.append(line.replaceAll("//.*$", " ") + "\n");
        }
        String cleanScript = sb.toString();

        // Remove new lines and normalize whitespace
        cleanScript = cleanScript.replaceAll("\\r?\\n", " ");
        cleanScript = cleanScript.replaceAll(" +", " ");

        for (String instruction : cleanScript.split(";")) {
            if (instruction.trim().startsWith("DECLARE")) {

                Matcher m = TYPES.matcher(instruction.trim());
                if (m.find()) {

                    String superType = CAS.TYPE_NAME_ANNOTATION;
                    try {
                        superType = m.group("superType").trim();
                    } catch (Exception e) {// ok, since might be absent
                    }

                    List<TypeFeatureDTO> features = list();
                    try {
                        // remove parenthesis, normalize whitespace and trim
                        String featuresStr = m.group("features").trim()
                                .replaceAll("[\\(\\)]", "")
                                .replaceAll(" +", " ").trim();

                        // split by comma, then space
                        for (String featureStr : featuresStr.split(",")) {
                            String[] split = featureStr.trim().split(" ");
                            validateArgument(split.length == 2,
                                    "features should come in the form of 'AnnotationType Identifier', was: "
                                            + split);
                            features.add(new TypeFeatureDTO(split[1].trim(),
                                    "", split[0].trim()));
                        }
                    } catch (NullPointerException e) {// not catching
                        // happens if m.group() not found when no features
                    }

                    String typeName = m.group("id");
                    types.add(new TypeDTO(typeName, "", superType, features));

                    try { // additional inline declarations of types
                        String inlineTypeName = m.group("inlineids");
                        for (String iid : inlineTypeName.substring(2).split(
                                ", ")) {
                            types.add(new TypeDTO(iid.trim(), "", superType,
                                    features));
                        }
                    } catch (Exception e) {// not catching
                        // happens if no additional inline declarations
                    }
                }
            }
        }
        return types;
    }

    /** DTO for UIMA annotation type */
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

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TypeDTO
                    && ((TypeDTO) obj).typeName.equals(typeName))
                return true;
            return false;
        }

        @Override
        public int hashCode() {
            return typeName.hashCode();
        }
    }

    /** DTO for UIMA annotation type feature */
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
