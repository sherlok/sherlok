package sherlok;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.collection.metadata.CpeDescriptorException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.InvalidXMLException;
import org.xml.sax.SAXException;

public class Pipeline {

    private String pipelineName;
    private String version;

    private List<AnalysisEngineDescription> aeds = new ArrayList<AnalysisEngineDescription>();
    private AnalysisEngine[] engines = null;

    private XmiCasSerializer xcs = new XmiCasSerializer(null);

    public Pipeline(String pipelineName, String version) {
        this.pipelineName = pipelineName;
        this.version = version;
    }

    public void add(AnalysisEngineDescription aDesc) throws IOException,
            SAXException, CpeDescriptorException, InvalidXMLException {
        if (engines != null)
            throw new IllegalArgumentException(
                    "cannot add more engines after first call to process()");
        aeds.add(aDesc);
    }

    public void initializeEngines() throws UIMAException {
        if (engines == null) {
            engines = createEngines(aeds
                    .toArray(new AnalysisEngineDescription[aeds.size()]));
        }
    }

    private static AnalysisEngine[] createEngines(
            AnalysisEngineDescription... descs) throws UIMAException {
        AnalysisEngine[] engines = new AnalysisEngine[descs.length];
        for (int i = 0; i < engines.length; ++i) {
            if (descs[i].isPrimitive()) {
                engines[i] = AnalysisEngineFactory.createEngine(descs[i]);
            } else {
                engines[i] = AnalysisEngineFactory.createEngine(descs[i]);
            }
        }
        return engines;
    }

    public String annotate(String text) throws UIMAException, SAXException {

        JCas jCas = Utils.getCas(text);
        System.out.println("annotating:: "+text);
        

        SimplePipeline.runPipeline(jCas, engines);

        StringWriter sw = new StringWriter();
        xcs.serializeJson(jCas.getCas(), sw);
        return sw.toString();
    }

    public void close() {
        for (AnalysisEngine engine : engines) {
            engine.destroy();
        }
    }

    @Override
    public String toString() {
        return pipelineName + ":" + version;
    }

}
