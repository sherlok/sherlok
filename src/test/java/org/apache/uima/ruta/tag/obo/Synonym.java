package org.apache.uima.ruta.tag.obo;

/**
 * A synonyn held in an OBO Ontology.
 */
public class Synonym {

    private String syn;
    private String type;
    private String source;

    public Synonym(String syn, String type, String source) {
        this.syn = syn;
        if (type != null && type.length() > 0)
            this.type = type;
        if (source != null && source.length() > 0)
            this.source = source;
    }

    /** @return The synonym string */
    public String getSyn() {
        return syn;
    }

    /** @return The type of the synonym */
    public String getType() {
        return type;
    }

    /** @return The source of the synonym */
    public String getSource() {
        return source;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        sb.append(syn);
        if (type != null)
            sb.append(", type=" + type);
        if (source != null)
            sb.append(", source=" + source);
        sb.append("]");
        return sb.toString();
    }
}
