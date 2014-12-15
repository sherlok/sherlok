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
package org.sherlok.aether;

import static org.slf4j.LoggerFactory.getLogger;

import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.graph.DependencyVisitor;
import org.slf4j.Logger;

/**
 * A dependency visitor that logs the graph.
 * 
 * @author renaud@apache.org
 */
public class ConsoleDependencyGraphDumper implements DependencyVisitor {
    private static final Logger LOG = getLogger(ConsoleDependencyGraphDumper.class);

    private String currentIndent = "";

    public boolean visitEnter(DependencyNode node) {
        LOG.debug(currentIndent + node);
        if (currentIndent.length() <= 0) {
            currentIndent = "+- ";
        } else {
            currentIndent = "|  " + currentIndent;
        }
        return true;
    }

    public boolean visitLeave(DependencyNode node) {
        currentIndent = currentIndent.substring(3, currentIndent.length());
        return true;
    }
}
