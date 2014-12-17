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

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.sherlok.SherlokServer.ANNOTATE;
import static org.sherlok.SherlokServer.DEFAULT_IP;
import static org.sherlok.SherlokServer.STATUS_OK;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import spark.StopServer;

/**
 * Integration tests for neuroner.
 *
 * @author renaud@apache.org
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RutaIntegrationTest {

    static final int TEST_PORT = 9609;
    static final String API_URL = "http://localhost:" + TEST_PORT + "/";
    public static final String TEST_TEXT = "Layer V and layer iii large pyramidal neurons. slowly adapting stretch receptor neuron";

    @Rule
    public MethodNameLoggerWatcher mdlw = new MethodNameLoggerWatcher();

    // @BeforeClass
    public static void beforeClass() throws Exception {
        SherlokServer.init(TEST_PORT, DEFAULT_IP);
    }

    // @AfterClass
    public static void afterClass() throws InterruptedException {
        System.err.println("STOP SERVER");
        StopServer.stop();
    }

    @Test
    public void test010_NeuronerAnnotate() throws Exception {

        for (int i = 0; i < 20; i++) {

            beforeClass();

            given().param("text", TEST_TEXT).when()
                    .get(API_URL + ANNOTATE + "/neuroner").then().log()
                    .everything().contentType(JSON).statusCode(STATUS_OK)
                    .body(containsString(TEST_TEXT))
                    .body("annotations.283.@type", equalTo("Layer"));
            /*-
            "283": {
                "@type": "Layer",
                "sofa": 1,
                "end": 7
            },
             */

            afterClass();
        }
    }
}
