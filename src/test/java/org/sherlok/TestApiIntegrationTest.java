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
package org.sherlok;

import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.sherlok.SherlokServer.DEFAULT_IP;
import static org.sherlok.SherlokServer.STATUS_INVALID;
import static org.sherlok.SherlokServer.STATUS_OK;
import static org.sherlok.SherlokServer.TEST;
import static org.sherlok.utils.Create.map;

import java.io.File;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.sherlok.mappings.PipelineDef;
import org.sherlok.mappings.PipelineDef.PipelineTest;
import org.sherlok.mappings.PipelineDef.TestAnnotation;

import spark.StopServer;

/**
 * Integration tests for pipeline REST API. This runs in a separate Spark server
 * on another port. However, ATM it share the same config, so stuff created
 * should be deleted after a test.
 * 
 * @author renaud@apache.org
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestApiIntegrationTest {

    static final int TEST_PORT = 9616;
    static final String API_URL = "http://localhost:" + TEST_PORT + "/" + TEST;

    @Rule
    public MethodNameLoggerWatcher mdlw = new MethodNameLoggerWatcher();

    @BeforeClass
    public static void beforeClass() throws Exception {
        Thread.sleep(250);
        SherlokServer.init(TEST_PORT, DEFAULT_IP);
        Thread.sleep(250);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        StopServer.stop();
    }

    @Test
    public void test010TestPipelines() {
        get(API_URL + "/01.ruta.annotate.dog").then().log().everything()
                .statusCode(STATUS_OK).contentType(JSON)//
                .body("status", equalTo("passed"));
    }

    @Test
    public void test020TestPipelineAsString() throws Exception {

        PipelineDef pipeline = FileBased.read(new File(
                FileBased.PIPELINES_PATH,
                "examples/01.ruta.annotate.dog_1.json"), PipelineDef.class);

        // original tests from pipeline work
        given().content(FileBased.writeAsString(pipeline))//
                .when().post(API_URL)//
                .then().log().everything().statusCode(STATUS_OK);

        // adding an OK test is OK
        List<PipelineTest> tests = pipeline.getTests();
        tests.add(new PipelineTest().setInput("another dog").setExpected(
                map("1",
                        new TestAnnotation().setBegin(8).setEnd(11)
                                .setType("Dog"))));
        given().content(FileBased.writeAsString(pipeline))//
                .when().post(API_URL)//
                .then().log().everything().statusCode(STATUS_OK);

        // adding a faulty test makes it fail
        tests.add(new PipelineTest().setInput("a cat").setExpected(
                map("1",
                        new TestAnnotation().setBegin(1).setEnd(2)
                                .setType("Cat"))));
        given().content(FileBased.writeAsString(pipeline))//
                .when().post(API_URL)//
                .then().log().everything().statusCode(STATUS_INVALID);
    }
}
