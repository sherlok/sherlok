package org.sherlok.pipelinetests;

import org.junit.Ignore;
import org.junit.Test;
import org.sherlok.SherlokPipelineTest;

/**
 * Test for pipeline stanford.sentiment.en:1.7.0. 
 *  Generated automatically by SherlokPipelineTest.main
 */
public class Pipeline_stanford_sentiment_en_1_7_0Test {

    @Test
    @Ignore
    public void test() throws Exception {
        SherlokPipelineTest.testPipeline("stanford.sentiment.en:1.7.0");
    }

}
//org.sherlok.mappings.SherlokException: could not find expected annotation StanfordSentimentAnnotation[0-11]
//        at org.sherlok.utils.SherlokTests.assertEquals(SherlokTests.java:90)
//        at org.sherlok.SherlokPipelineTest.testPipeline(SherlokPipelineTest.java:59)
//        at org.sherlok.pipelinetests.Pipeline_stanford_sentiment_en_1_7_0Test.test(Pipeline_stanford_sentiment_en_1_7_0Test.java:14)
//        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
//        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
//        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
//        at java.lang.reflect.Method.invoke(Method.java:601)
//        at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:50)
//        at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
//        at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47)
//        at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
//        at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:325)
//        at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:78)
//        at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:57)
//        at org.junit.runners.ParentRunner$3.run(ParentRunner.java:290)
//        at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:71)
//        at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:288)
//        at org.junit.runners.ParentRunner.access$000(ParentRunner.java:58)
//        at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:268)
//        at org.junit.runners.ParentRunner.run(ParentRunner.java:363)
//        at org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run(JUnit4TestReference.java:50)
//        at org.eclipse.jdt.internal.junit.runner.TestExecution.run(TestExecution.java:38)
//        at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:459)
//        at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:675)
//        at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.run(RemoteTestRunner.java:382)
//        at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.main(RemoteTestRunner.java:192)
//
