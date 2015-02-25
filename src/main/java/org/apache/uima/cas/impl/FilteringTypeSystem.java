/**
 * Copyright (C) 2014-2015 Renaud Richardet
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
package org.apache.uima.cas.impl; // needed because of methods visibility

import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.admin.CASAdminException;
import org.sherlok.UimaPipeline;

/**
 * Used to filter JSON writer, see {@link UimaPipeline}
 * {@link XmiCasSerializer#setFilterTypes(TypeSystemImpl)}. Requires to be in
 * this package because of package visibility
 * 
 * @author renaud@apache.org
 */
public class FilteringTypeSystem extends TypeSystemImpl {

    /**
     * @param type
     *            the type to include in the JSON output
     */
    public void includeType(Type type) {
        int typeId = super.addType(type.getName(), annotBaseTypeCode);
        for (Feature f : type.getFeatures()) {
            try {
                super.addFeature(f.getShortName(), typeId, annotBaseTypeCode);
            } catch (CASAdminException cae) {
                if (!cae.getArguments()[0].equals("sofa")) {
                    throw new RuntimeException(cae);
                }
            }
        }
    }

    /*-
    // for debugging:
    public Type getType(String typeName) {
        System.err.println("Type:: " + typeName);
        return super.getType(typeName);
    }

    public Type getArrayType(Type componentType) {
        System.err.println("ArrayType:: " + componentType);
        return super.getArrayType(componentType);
    }

    public Feature getFeatureByFullName(String featureName) {
        System.err.println("FeatureByFullName:: " + featureName);
        return super.getFeatureByFullName(featureName);
    }

    public Iterator<Type> getTypeIterator() {
        System.err.println("TypeIterator");
        return super.getTypeIterator();
    }

    public Type getTopType() {
        System.err.println("TopType");
        return super.getTopType();
    }

    public Vector<Type> getDirectlySubsumedTypes(Type type) {
        System.err.println("DirectlySubsumedTypes:: " + type);
        return super.getDirectlySubsumedTypes(type);
    }

    public List<Type> getDirectSubtypes(Type type) {
        System.err.println("DirectSubtypes:: " + type);
        return super.getDirectSubtypes(type);
    }

    public List<Type> getProperlySubsumedTypes(Type type) {
        System.err.println("ProperlySubsumedTypes:: " + type);
        return super.getProperlySubsumedTypes(type);
    }

    public Type getParent(Type type) {
        System.err.println("Parent:: " + type);
        return super.getParent(type);
    }

    public boolean subsumes(Type superType, Type subType) {
        System.err.println("subsumes:: " + subType);
        return super.subsumes(superType, subType);
    }

    public Iterator<Feature> getFeatures() {
        System.err.println("Features");
        return super.getFeatures();
    }

    public TypeNameSpace getTypeNameSpace(String name) {
        System.err.println("TypeNameSpace:: " + name);
        return super.getTypeNameSpace(name);
    }

    public LowLevelTypeSystem getLowLevelTypeSystem() {
        System.err.println("LowLevelTypeSystem");
        return super.getLowLevelTypeSystem();
    }
     */
}
