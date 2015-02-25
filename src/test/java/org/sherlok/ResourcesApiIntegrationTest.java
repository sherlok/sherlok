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
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.containsString;
import static org.sherlok.SherlokServer.DEFAULT_IP;
import static org.sherlok.SherlokServer.STATUS_OK;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;

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
            + SherlokServer.RUTA_RESOURCES;

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
    public void test010GetResources() {
        get(API_URL).then().log().everything()//
                .statusCode(STATUS_OK)//
                .contentType(JSON)//
                .content(containsString("countries.txt"));
    }

    @Test
    public void test020GetResource() {
        get(API_URL + "/countries.txt").then().log().everything()//
                .contentType("application/octet-stream").statusCode(STATUS_OK);
        // TODO more assertions
    }

    // TODO more tests

}
