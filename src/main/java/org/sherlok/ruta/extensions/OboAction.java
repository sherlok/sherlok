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
package org.sherlok.ruta.extensions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.ruta.RutaStream;
import org.apache.uima.ruta.action.AbstractRutaAction;
import org.apache.uima.ruta.expression.number.INumberExpression;
import org.apache.uima.ruta.rule.RuleElement;
import org.apache.uima.ruta.rule.RuleMatch;
import org.apache.uima.ruta.visitor.InferenceCrowd;

/**
 * 
 * @see MarkFastAction
 * 
 * @author richarde
 */
public class OboAction extends AbstractRutaAction {

    private List<INumberExpression> indexExprList;
    @SuppressWarnings("unused")
    private String engineName;

    public OboAction(String engineName) {
        super();
        this.engineName = engineName;
    }

    @Override
    public void execute(RuleMatch match, RuleElement element,
            RutaStream stream, InferenceCrowd crowd) {
        List<Integer> indexes = new ArrayList<Integer>();
        // for (INumberExpression each : indexExprList) {
        // RutaBlock parent = element.getParent();
        // int integerValue = each.getIntegerValue(parent, match, element,
        // stream);
        // indexes.add(integerValue);
        // }
        List<RuleElement> ruleElements = element.getContainer()
                .getRuleElements();
        for (Integer each : indexes) {
            if (each > 0 && each <= ruleElements.size()) {
                Type type = getRandomType(stream);
                RuleElement ruleElement = ruleElements.get(each - 1);
                List<AnnotationFS> matchedAnnotationsOf = match
                        .getMatchedAnnotationsOf(ruleElement);
                for (AnnotationFS eachMatched : matchedAnnotationsOf) {
                    AnnotationFS newAFS = stream.getCas().createAnnotation(
                            type, eachMatched.getBegin(), eachMatched.getEnd());
                    stream.addAnnotation(newAFS, true, match);
                }
            }
        }
    }

    private Type getRandomType(RutaStream stream) {
        Type annotationType = stream.getCas().getTypeSystem()
                .getType("org.apache.uima.ruta.type.TokenSeed");
        TypeSystem typeSystem = stream.getCas().getTypeSystem();
        List<Type> subsumedTypes = typeSystem
                .getProperlySubsumedTypes(annotationType);
        Random r = new Random();
        int nextInt = r.nextInt(subsumedTypes.size());
        return subsumedTypes.get(nextInt);
    }

    public List<INumberExpression> getIndexExprList() {
        return indexExprList;
    }
    //
    // public void setIndexExprList(List<INumberExpression> indexExprList) {
    // this.indexExprList = indexExprList;
    // }

}
