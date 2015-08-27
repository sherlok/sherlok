package org.sherlok.pipelinetests;

import org.junit.Test;
import org.sherlok.SherlokPipelineTest;

/**
 * Test for pipeline ${pipelineId}. 
 *  Generated automatically by SherlokPipelineTest.main
 */
public class ${className} {

    @Rule
    public MethodNameLoggerWatcher mdlw = new MethodNameLoggerWatcher();

    @Test
    public void test${className}() throws Exception {
        SherlokPipelineTest.testPipeline("${pipelineId}");
    }

}
