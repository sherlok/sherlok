package org.apache.uima.cas.impl; // needed because of methods visibility

import org.sherlok.Pipeline;

/**
 * Used to filter JSON writer, see {@link Pipeline}
 * {@link XmiCasSerializer#setFilterTypes(TypeSystemImpl)}
 * 
 * @author richarde
 */
public class FilteringTypeSystem extends TypeSystemImpl {

    /**
     * @param typeName
     *            the type to include in the JSON output
     * @param properties
     *            the name of properties to include in the JSON output
     */
    public void includeType(String typeName, String... properties) {
        int type = super.addType(typeName, annotBaseTypeCode);
        for (String property : properties) {
            super.addFeature(property, type, annotBaseTypeCode);
        }
    }
    /*-

    public Type getType(String typeName) {
        System.err.println("XXX Type:: " + typeName);
        return super.getType(typeName);
    }

    public Type getArrayType(Type componentType) {
        System.err.println("XXX ArrayType:: " + componentType);
        return super.getArrayType(componentType);
    }

    public Feature getFeatureByFullName(String featureName) {
        System.err.println("XXX FeatureByFullName:: " + featureName);
        return super.getFeatureByFullName(featureName);
    }

    public Iterator<Type> getTypeIterator() {
        System.err.println("XXX TypeIterator");
        return super.getTypeIterator();
    }

    public Type getTopType() {
        System.err.println("XXX TopType");
        return super.getTopType();
    }

    public Vector<Type> getDirectlySubsumedTypes(Type type) {
        System.err.println("XXX DirectlySubsumedTypes:: " + type);
        return super.getDirectlySubsumedTypes(type);
    }

    public List<Type> getDirectSubtypes(Type type) {
        System.err.println("XXX DirectSubtypes:: " + type);
        return super.getDirectSubtypes(type);
    }

    public List<Type> getProperlySubsumedTypes(Type type) {
        System.err.println("XXX ProperlySubsumedTypes:: " + type);
        return super.getProperlySubsumedTypes(type);
    }

    public Type getParent(Type type) {
        System.err.println("XXX Parent:: " + type);
        return super.getParent(type);
    }

    public boolean subsumes(Type superType, Type subType) {
        System.err.println("XXX subsumes:: " + subType);
        return super.subsumes(superType, subType);
    }

    public Iterator<Feature> getFeatures() {
        System.err.println("XXX Features");
        return super.getFeatures();
    }

    public TypeNameSpace getTypeNameSpace(String name) {
        System.err.println("XXX TypeNameSpace:: " + name);
        return super.getTypeNameSpace(name);
    }

    public LowLevelTypeSystem getLowLevelTypeSystem() {
        System.err.println("XXX LowLevelTypeSystem");
        return super.getLowLevelTypeSystem();
    }
     */

}