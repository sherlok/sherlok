package org.sherlok;

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
