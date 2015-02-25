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
package org.sherlok.uima.engines;

import static com.google.common.collect.Sets.newHashSet;
import static org.apache.uima.fit.util.CasUtil.getType;
import static org.apache.uima.fit.util.CasUtil.selectCovered;
import static org.apache.uima.fit.util.JCasUtil.selectAll;

import java.util.List;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.sherlok.utils.Create;

/**
 * Prunes/dedupes overlapping Annotations, keeps the largest one.
 * 
 * @see DeduplicatorAnnotator for simpler algo
 * @see ViterbiAnnotator for complex tasks and multiple annotation types
 * 
 * @author renaud@apache.org
 */
public class KeepLargestAnnotationAnnotator extends JCasAnnotator_ImplBase {

    public static final String PARAM_ANNOTATION_CLASS = "annotationClass";
    @ConfigurationParameter(name = PARAM_ANNOTATION_CLASS, mandatory = true, //
    description = "the full name of the annotation class")
    private String annotationClassStr;

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {

        Type type = getType(jCas.getCas(), annotationClassStr);

        List<Annotation> annots = Create.list();
        for (TOP a : selectAll(jCas)) {
            if (a.getType().equals(type) && a instanceof Annotation) {
                annots.add((Annotation) a);
            }
        }
        System.err.println("annots: " + annots.size());

        if (annots.size() > 1) {

            Set<AnnotationFS> toDelete = newHashSet();

            for (int i = 0; i < annots.size(); i++) {
                AnnotationFS outer = annots.get(i);
                if (!toDelete.contains(outer)) {
                    toDelete.addAll(selectCovered(type, outer));
                }
            }
            System.err.println("del:" + toDelete.size());
            Annotation[] arr = toDelete
                    .toArray(new Annotation[toDelete.size()]);
            for (int i = 0; i < arr.length; i++) {
                arr[i].removeFromIndexes(jCas);
            }
        }
    }
}