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
package org.sherlok;

import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.*;
import static org.sherlok.SherlokServer.DEFAULT_IP;
import static org.sherlok.SherlokServer.PIPELINES;
import static org.sherlok.SherlokServer.STATUS_INVALID;
import static org.sherlok.SherlokServer.STATUS_MISSING;
import static org.sherlok.SherlokServer.STATUS_OK;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.sherlok.mappings.PipelineDef;

import spark.StopServer;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Integration tests for pipeline REST API. This runs in a separate Spark server
 * on another port. However, ATM it share the same config, so stuff created
 * should be deleted after a test.
 * 
 * @author renaud@apache.org
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PipelineApiIntegrationTest {

    static final int TEST_PORT = 9606;
    static final String API_URL = "http://localhost:" + TEST_PORT + "/"
            + PIPELINES;

    @Rule
    public MethodNameLoggerWatcher mdlw = new MethodNameLoggerWatcher();

    @BeforeClass
    public static void beforeClass() throws Exception {
        SherlokServer.init(TEST_PORT, DEFAULT_IP);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        StopServer.stop();
    }

    @Test
    public void test010GetPipelines() {
        get(API_URL)
                .then()
                .log()
                .everything()
                .contentType(JSON)
                .statusCode(STATUS_OK)
                .content(containsString("opennlp.ners.en"))
                .content(
                        containsString("\"description\" : \"annotates English persons and locations using OpenNLP models\","));
    }

    @Test
    public void test020GetPipeline() {
        get(API_URL + "/opennlp.ners.en/1.6.2")
                .then()
                .log()
                .everything()
                .contentType(JSON)
                .statusCode(STATUS_OK)
                .body("name", equalTo("opennlp.ners.en"))
                .body("version", equalTo("1.6.2"))
                .body("loadOnStartup", equalTo(false))
                .body("script[0]",
                        equalTo("ENGINE opennlp.segmenter.en:1.6.2;"));
    }

    @Test
    /** Let's put a new test pipeline*/
    public void test030PutPipeline() throws JsonProcessingException {
        PipelineDef e = new PipelineDef().setDomain("test").addScriptLine(
                "ENGINE sample.engine:1");
        e.setName("test");
        e.setVersion("1");
        String testPipelineDef = FileBased.writeAsString(e);

        given().content(testPipelineDef)//
                .when().put(API_URL)//
                .then().log().everything().statusCode(STATUS_OK);
    }

    @Test
    /** Let's put a new test pipeline for TEST_ONLY */
    public void test031PutPipelineTestOnly() throws JsonProcessingException {

        String name = PipelineApiIntegrationTest.class.getSimpleName()
                + "_test";
        PipelineDef pd = new PipelineDef().setDomain("test").addScriptLine(
                "DECLARE Dog;");
        pd.setName(name);
        pd.setVersion("1");
        String pdJson = FileBased.writeAsString(pd);

        given().content(pdJson).when()
                .put(API_URL + "?" + SherlokServer.TEST_ONLY + "=true")//
                .then().log().everything().statusCode(STATUS_OK);

        // pipeline should not be here, since PUT was TEST_ONLY
        get(API_URL + PIPELINES + "/" + name + "/" + 1).then().log()
                .everything().statusCode(STATUS_MISSING);
    }

    @Test
    /** Let's put a FAULTY test pipeline for test */
    public void test032PutFaultyPipelineTestOnly()
            throws JsonProcessingException {

        String name = PipelineApiIntegrationTest.class.getSimpleName()
                + "_test_faulty";
        PipelineDef pd = new PipelineDef().setDomain("test").addScriptLine(
                "DECL_wrong_ARE Dog;");
        pd.setName(name);
        pd.setVersion("1");
        String pdJson = FileBased.writeAsString(pd);

        given().content(pdJson).when()
                .put(API_URL + "?" + SherlokServer.TEST_ONLY + "=true")//
                .then().log().everything().statusCode(STATUS_INVALID);
    }

    @Test
    /** Putting a faulty pipeline should fail */
    public void test031PutFaultyPipeline() throws JsonProcessingException {
        given().content("blah")//
                .when().put(API_URL)//
                .then().log().everything().statusCode(STATUS_INVALID);
    }

    @Test
    /** Putting a faulty pipeline should fail (id is missing) */
    public void test032PutFaultyPipeline() throws JsonProcessingException {
        given().content("{  \"name\" : \"opennlp.ners.en\"}")//
                .when().put(API_URL)//
                .then().log().everything().statusCode(STATUS_INVALID);
    }

    @Test
    /** .. and check that the new test pipeline is here */
    public void test040GetTestPipeline() {
        get(API_URL + "/test/1").then().log().everything()//
                .contentType(JSON).statusCode(STATUS_OK)//
                .body("name", equalTo("test"))//
                .body("version", equalTo("1"))//
                .body("script[0]", equalTo("ENGINE sample.engine:1"));
    }

    @Test
    /** .. and check that a bogus pipelien is NOT here */
    public void test041GetFaultyTestPipeline() {
        get(API_URL + "/test/1000000000198198")//
                .then().log().everything().statusCode(STATUS_INVALID);
    }

    @Test
    /** Now let's delete the test pipeline*/
    public void test050DeletePipeline() throws JsonProcessingException {
        when().delete(API_URL + "/test/1").//
                then().log().everything().statusCode(STATUS_OK);
    }

    @Test
    /** ... and check that it is gone. */
    public void test060GetTestPipelineGone() {
        get(API_URL + "/test/1")//
                .then().log().everything().statusCode(STATUS_INVALID);
    }
}
