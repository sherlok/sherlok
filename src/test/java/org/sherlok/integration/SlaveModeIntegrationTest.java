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
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.sherlok.SherlokServer.ANNOTATE;
import static org.sherlok.SherlokServer.DEFAULT_IP;
import static org.sherlok.SherlokServer.PIPELINES;
import static org.sherlok.SherlokServer.STATUS_INVALID;
import static org.sherlok.SherlokServer.STATUS_OK;
import static org.sherlok.integration.PipelineLoaderIntegrationTest.TEST_TEXT;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.sherlok.FileBased;
import org.sherlok.SherlokServer;
import org.sherlok.mappings.PipelineDef;

import spark.StopServer;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Integration tests for slave mode. <br>
 * TODO Ignore'd for now since it needs another Sherlok server running as
 * master, and {@link SherlokServer#init()} calls static methods..<br>
 * Workaround: manuall start the master with
 * <code>sh start_server.sh -port 9607</code>
 *
 * @author renaud@apache.org
 */
@Ignore
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SlaveModeIntegrationTest {
    // FIXME must test with disabled config

    static final int MASTER_PORT = 9607;
    static final int SLAVE_PORT = 9617;
    static final String SLAVE_API_URL = "http://localhost:" + SLAVE_PORT + "/";
    static final String MASTER_API_URL = "http://localhost:" + MASTER_PORT
            + "/";

    @Rule
    public MethodNameLoggerWatcher mdlw = new MethodNameLoggerWatcher();

    @BeforeClass
    public static void beforeClass() throws Exception {
        Thread.sleep(250);
        SherlokServer.init(SLAVE_PORT, DEFAULT_IP, MASTER_API_URL, false);
        Thread.sleep(250);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        StopServer.stop();
    }

    @Test
    public void test010_startupMasterAndSlave() {
    }

    @Test
    public void test011_Annotate() {
        given().param("text", TEST_TEXT).when()
                .get(SLAVE_API_URL + ANNOTATE + "/opennlp.ners.en").then()
                .log().everything().statusCode(STATUS_OK).contentType(JSON)
                .body(containsString(TEST_TEXT))//
                .body("annotations.1009.value", equalTo("person"));
    }

    @Test
    /** PUT should return status 400*/
    public void test030PutPipeline() throws JsonProcessingException {
        PipelineDef p = (PipelineDef) new PipelineDef().addScriptLine(
                "ENGINE sample.engine:1").setDomain("test");
        p.setName("test");
        p.setVersion("1");

        given().content(FileBased.writeAsString(p))//
                .when().put(SLAVE_API_URL + PIPELINES)//
                .then().log().everything().statusCode(STATUS_INVALID);
    }
    /*-

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
    }*/
}
