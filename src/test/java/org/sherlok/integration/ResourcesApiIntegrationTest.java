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

import static com.google.common.io.Files.write;
import static com.jayway.restassured.RestAssured.delete;
import static com.jayway.restassured.RestAssured.*;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static java.io.File.createTempFile;
import static java.nio.charset.Charset.defaultCharset;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.sherlok.SherlokServer.DEFAULT_IP;
import static org.sherlok.SherlokServer.RUTA_RESOURCES;
import static org.sherlok.SherlokServer.STATUS_MISSING;
import static org.sherlok.SherlokServer.*;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.sherlok.SherlokServer;

import spark.StopServer;

/**
 * Integration tests for resources REST API. This runs in a separate Spark
 * server on another port. However, ATM it share the same config, so stuff
 * created should be deleted after a test.
 * 
 * @author renaud@apache.org
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ResourcesApiIntegrationTest {

    static final int TEST_PORT = 9610;
    static final String API_URL = "http://localhost:" + TEST_PORT + "/"
            + RUTA_RESOURCES;

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
    public void test010GetResources() {
        get(API_URL).then().log().everything()//
                .statusCode(STATUS_OK)//
                .contentType(JSON)//
                .content(containsString("countries.txt"));
    }

    @Test
    public void test020GetResource() {
        get(API_URL + "/countries.txt").then().log().everything()
                .contentType("application/octet-stream").statusCode(STATUS_OK)
                .body(containsString("Afghanistan"));
    }

    final static String path = "/test/test030PostResource.txt";

    @Test
    public void test030PostTestResource() throws Exception {

        File tmpFile = createTempFile("test030PostResource", "txt");
        write("hello world", tmpFile, defaultCharset());

        given().multiPart(tmpFile)//
                .when().post(API_URL + path)//
                .then().log().everything().statusCode(STATUS_OK);
    }

    @Test
    public void test031PostWrongTestResourceWithDots() throws Exception {

        File tmpFile = createTempFile("test031PostWrongTestResource", "txt");
        write("hello world", tmpFile, defaultCharset());

        given().multiPart(tmpFile)//
                .when().post(API_URL + "/.." + path)//
                .then().log().everything().statusCode(STATUS_INVALID);
    }

    @Test
    public void test032PostWrongEmptyTestResource() throws Exception {

        File tmpFile = createTempFile("test032PostWrongEmptyTestResource",
                "txt");

        given().multiPart(tmpFile)//
                .when().post(API_URL + "/.." + path)//
                .then().log().everything().statusCode(STATUS_INVALID);
    }

    @Test
    public void test033PostWrongNoTestResource() throws Exception {
        post(API_URL + "/.." + path)//
                .then().log().everything().statusCode(STATUS_INVALID);
    }

    @Test
    public void test040GetTestResource() throws Exception {
        get(API_URL + path).then().log().everything().statusCode(STATUS_OK)
                .body(equalTo("hello world"));
    }

    @Test
    public void test050DeleteResource() throws Exception {
        delete(API_URL + path)//
                .then().log().everything().statusCode(STATUS_OK);
    }

    @Test
    public void test060ResourceShouldBeGone() throws Exception {
        get(API_URL + path).then().log().everything()
                .statusCode(STATUS_MISSING);
    }
}
