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

import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static com.jayway.restassured.http.ContentType.JSON;
import static java.lang.System.currentTimeMillis;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.sherlok.SherlokServer.DEFAULT_IP;
import static org.sherlok.SherlokServer.PIPELINES;
import static org.sherlok.SherlokServer.STATUS_INVALID;
import static org.sherlok.SherlokServer.STATUS_OK;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.sherlok.FileBased;
import org.sherlok.SherlokServer;
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
        Thread.sleep(250);
        SherlokServer.init(TEST_PORT, DEFAULT_IP, null, null);
        Thread.sleep(250);
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
                .statusCode(STATUS_OK)
                .contentType(JSON)
                .content(containsString("opennlp.ners.en"))
                .content(
                        containsString("\"description\" : \"annotates English persons and locations using OpenNLP models\","));
    }

    @Test
    public void test020GetPipeline() {
        get(API_URL + "/opennlp.ners.en/1.7.0")
                .then()
                .log()
                .everything()
                .statusCode(STATUS_OK)
                .contentType(JSON)
                .body("name", equalTo("opennlp.ners.en"))
                .body("version", equalTo("1.7.0"))
                .body("script[0]",
                        equalTo("ENGINE opennlp.segmenter.en:1.7.0;"));
    }

    @Test
    /** Let's put a new test pipeline. It gets deleted below... */
    public void test030PostPipeline() throws JsonProcessingException {
        PipelineDef p = (PipelineDef) new PipelineDef().addScriptLine(
                "ENGINE languageidentifier:1.7.0").setDomain("test");
        p.setName("test");
        p.setVersion("1");

        given().content(FileBased.writeAsString(p))//
                .when().post(API_URL)//
                .then().log().everything().statusCode(STATUS_OK);
    }

    @Test
    /** Post'ing a faulty pipeline should fail  */
    public void test031PostFaultyPipeline() throws JsonProcessingException {
        given().content("blah")//
                .when().post(API_URL)//
                .then().log().everything().statusCode(STATUS_INVALID);
    }

    @Test
    /** Post'ing a faulty pipeline should fail (ENGINE does not exsist) */
    public void test032PostFaultyPipeline() throws JsonProcessingException {
        PipelineDef p = (PipelineDef) new PipelineDef()
                .addScriptLine("ENGINE nonexisting.engine:"
                        + currentTimeMillis());
        p.setName("test_" + currentTimeMillis());
        p.setVersion("1");

        given().content(FileBased.writeAsString(p))//
                .when().post(API_URL)//
                .then().log().everything().statusCode(STATUS_INVALID);
    }

    @Test
    /** Post'ing a faulty pipeline should fail (id is missing) */
    public void test033PostFaultyPipeline() throws JsonProcessingException {
        given().content("{  \"name\" : \"opennlp.ners.en\"}")//
                .when().post(API_URL)//
                .then().log().everything().statusCode(STATUS_INVALID);
    }

    @Test
    /** .. and check that the new test pipeline is here */
    public void test040GetTestPipeline() {
        get(API_URL + "/test/1").then().log().everything()//
                .statusCode(STATUS_OK)//
                .contentType(JSON)//
                .body("name", equalTo("test"))//
                .body("version", equalTo("1"))//
                .body("script[0]", equalTo("ENGINE languageidentifier:1.7.0"));
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
