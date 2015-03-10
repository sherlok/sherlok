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

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.sherlok.SherlokServer.ANNOTATE;
import static org.sherlok.SherlokServer.DEFAULT_IP;
import static org.sherlok.SherlokServer.STATUS_INVALID;
import static org.sherlok.SherlokServer.STATUS_OK;
import static org.sherlok.integration.PipelineLoaderIntegrationTest.TEST_TEXT;
import static org.sherlok.mappings.SherlokResult.parse;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.sherlok.SherlokServer;
import org.sherlok.mappings.JsonAnnotation;
import org.sherlok.mappings.SherlokResult;

import spark.StopServer;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Integration tests for annotation REST API. This runs in a separate Spark
 * server on another port. However, ATM it share the same config, so stuff
 * created should be deleted after a test.
 *
 * @author renaud@apache.org
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AnnotationApiIntegrationTest {

    static final int TEST_PORT = 9605;
    static final String API_URL = "http://localhost:" + TEST_PORT + "/"
            + ANNOTATE;

    @Rule
    public MethodNameLoggerWatcher mdlw = new MethodNameLoggerWatcher();

    @BeforeClass
    public static void beforeClass() throws Exception {
        Thread.sleep(250);
        SherlokServer.init(TEST_PORT, DEFAULT_IP, null, false);
        Thread.sleep(250);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        StopServer.stop();
    }

    @Test
    public void test010_GETAnnotate() throws Exception {
        String json = given().param("text", TEST_TEXT).when()
                .get(API_URL + "/opennlp.ners.en").then().log().everything()
                .statusCode(STATUS_OK).contentType(JSON)
                .body(containsString(TEST_TEXT))//
                .extract().asString();
        List<JsonAnnotation> ne = SherlokResult.parse(json).get("NamedEntity");
        assertEquals(3, ne.size());
    }

    @Test
    public void test011_POSTAnnotate() throws Exception {
        String json = given().param("text", TEST_TEXT).when()
                .post(API_URL + "/opennlp.ners.en")//
                .then().log().everything()//
                .statusCode(STATUS_OK)//
                .contentType(JSON)//
                .body(containsString(TEST_TEXT))//
                .extract().asString();
        assertEquals(3, parse(json).get("NamedEntity").size());

        // same POST to check multiple calls
        json = given().param("text", TEST_TEXT).when()
                .post(API_URL + "/opennlp.ners.en").then().log().everything()
                .statusCode(STATUS_OK).contentType(JSON)
                .body(containsString(TEST_TEXT))//
                .extract().asString();
        assertEquals(3, parse(json).get("NamedEntity").size());
    }

    @Test
    public void test012WrongPipeline() {
        given().param("text", TEST_TEXT) //
                .when()//
                .post(API_URL + "/blablabla")//
                .then().log().everything()//
                .statusCode(STATUS_INVALID)//
                .contentType(JSON);
    }

    @Test
    public void test020MissingText() throws JsonProcessingException {
        when().post(API_URL + "/opennlp.ners.en")//
                .then().log().everything()//
                .statusCode(STATUS_INVALID);
    }

    @Test
    public void test021EmptyText() throws JsonProcessingException {
        given().param("text", "").when()//
                .post(API_URL + "/opennlp_en_ners")//
                .then().log().everything()//
                .contentType(JSON).statusCode(STATUS_INVALID);
    }
}
