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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.sherlok.FileBased.writeAsString;
import static org.sherlok.SherlokServer.BUNDLES;
import static org.sherlok.SherlokServer.DEFAULT_IP;
import static org.sherlok.SherlokServer.STATUS_INVALID;
import static org.sherlok.SherlokServer.STATUS_OK;
import static org.sherlok.mappings.BundleDef.BundleDependency.DependencyType.mvn;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.sherlok.SherlokServer;
import org.sherlok.mappings.BundleDef;
import org.sherlok.mappings.BundleDef.BundleDependency;

import spark.StopServer;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Integration tests for bundle REST API. This runs in a separate Spark server
 * on another port. However, ATM it share the same config, so stuff created
 * should be deleted after a test.
 * 
 * @author renaud@apache.org
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BundleApiIntegrationTest {

    static final int TEST_PORT = 9608;
    static final String API_URL = "http://localhost:" + TEST_PORT + "/"
            + BUNDLES;

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
    public void test010GetBundles() {
        get(API_URL).then().log().everything()//
                .statusCode(STATUS_OK)//
                .contentType(JSON)//
                .content(containsString("\"name\" : \"dkpro.opennlp.en\","));
    }

    @Test
    public void test020GetBundle() {
        get(API_URL + "/dkpro.opennlp.en/1.7.0")
                .then()
                .log()
                .everything()
                .statusCode(STATUS_OK)
                .contentType(JSON)
                .body("name", equalTo("dkpro.opennlp.en"))
                .body("version", equalTo("1.7.0"))
                .body("repositories.dkpro",
                        equalTo("http://zoidberg.ukp.informatik.tu-darmstadt.de/artifactory/public-model-releases-local/"));
    }

    @Test
    /** Let's put a new test bundle*/
    public void test030PutBundle() throws JsonProcessingException {
        BundleDef e = new BundleDef()
                .addDependency(new BundleDependency(
                        mvn,
                        "de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.stanfordnlp-gpl:1.7.0"));
        e.setName("test");
        e.setVersion("172");

        given().content(writeAsString(e))//
                .when().put(API_URL)//
                .then().log().everything().statusCode(STATUS_OK);
    }

    @Test
    /** Putting a faulty bundle should fail */
    public void test031PutFaultyBundle() throws JsonProcessingException {
        given().content("blah")//
                .when().put(API_URL)//
                .then().log().everything().statusCode(STATUS_INVALID);
    }

    @Test
    /** Putting a faulty bundle should fail (id is missing) */
    public void test032PutFaultyBundle() throws JsonProcessingException {
        given().content("{  \"name\" : \"blabla\"}")//
                .when().put(API_URL)//
                .then().log().everything().statusCode(STATUS_INVALID);
    }

    @Test
    /** .. and check that the new test bundle is here */
    public void test040GetTestBundle() {
        get(API_URL + "/test/172")
                .then()
                .log()
                .everything()
                .statusCode(STATUS_OK)
                .contentType(JSON)
                .body("name", equalTo("test"))
                .body("version", equalTo("172"))
                .body("dependencies[0].type", equalTo("mvn"))
                .body("dependencies[0].value",
                        equalTo("de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.stanfordnlp-gpl:1.7.0"));
    }

    @Test
    /** .. and check that a bogus bundle is NOT here */
    public void test041GetFaultyTestBundle() {
        get(API_URL + "/test/1000000000198198")//
                .then().log().everything().statusCode(STATUS_INVALID);
    }

    @Test
    /** Now let's delete the test bundle */
    public void test050DeleteBundle() throws JsonProcessingException {
        when().delete(API_URL + "/test/172").//
                then().log().everything().statusCode(STATUS_OK);
    }

    @Test
    /** ... and check that it is gone. */
    public void test060GetTestBundleGone() {
        get(API_URL + "/test/172")//
                .then().log().everything().statusCode(STATUS_INVALID);
    }

    // // TODO
    // //
    // //

    /*-
    @Test
    
     Putting a faulty engine should fail 
    public void test031PutFaultyEngine() throws JsonProcessingException {
        given().content("blah")//
                .when().put(API_URL)//
                .then().log().everything().statusCode(STATUS_INVALID);
    }

    @Test
     Putting a faulty engine should fail (id is missing) 
    public void test032PutFaultyEngine() throws JsonProcessingException {
        given().content("{  \"name\" : \"blabla\"}")//
                .when().put(API_URL)//
                .then().log().everything().statusCode(STATUS_INVALID);
    }

    @Test
     .. and check that the new test engine is here 
    public void test040GetTestEngine() {
        get(API_URL + "/test/17")
                .then()
                .log()
                .everything()
                .contentType(JSON)
                .statusCode(STATUS_OK)
                .body("name", equalTo("test"))
                .body("version", equalTo("17"))
                .body("class",
                        equalTo("de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter"))
                .body("script", equalTo(null));
    }

    @Test
     .. and check that a bogus engine is NOT here 
    public void test041GetFaultyTestEngine() {
        get(API_URL + "/test/1000000000198198")//
                .then().log().everything().statusCode(STATUS_INVALID);
    }

     */

}
