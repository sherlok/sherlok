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
package org.sherlok.integration;

import static org.slf4j.LoggerFactory.getLogger;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;

/**
 * Prints the test's method name in the logs, useful for debugging integration
 * tests with lots of logs.
 * 
 * @see http://junit.org/javadoc/latest/org/junit/rules/TestWatcher.html
 * 
 * @author renaud@apache.org
 */
public class MethodNameLoggerWatcher extends TestWatcher {
    private static Logger LOG = getLogger(MethodNameLoggerWatcher.class);

    protected void starting(Description description) {
        LOG.debug("starting test " + description);
    };

}
