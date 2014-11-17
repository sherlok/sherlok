package org.sherlok;

import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.*;
import static org.sherlok.SherlokServer.PIPELINES;
import static org.sherlok.SherlokServer.STATUS_INVALID;
import static org.sherlok.SherlokServer.STATUS_OK;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.sherlok.mappings.PipelineDef;
import org.sherlok.mappings.PipelineDef.PipelineEngine;

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

    @BeforeClass
    public static void beforeClass() throws Exception {
        SherlokServer.init(TEST_PORT);
    }

    @AfterClass
    public static void afterClass() {
        StopServer.stop();
    }

    @Test
    public void test010GetPipelines() {
        get(API_URL)
                .then()
                .contentType(JSON)
                .statusCode(STATUS_OK)
                .content(containsString("opennlp.ners.en"))
                .content(
                        containsString("\"annotations\" : [ \"dkpro.NamedEntity\" ],"));
    }

    @Test
    public void test020GetPipeline() {
        get(API_URL + "/opennlp.ners.en/1.6.2").then()//
                .contentType(JSON).statusCode(STATUS_OK)//
                .body("name", equalTo("opennlp.ners.en"))//
                .body("version", equalTo("1.6.2"))//
                .body("loadOnStartup", equalTo(true))//
                .body("engines[0].id", equalTo("opennlp.segmenter.en:1.6.2"))//
                .body("engines[0].script", equalTo(null));
    }

    @Test
    /** Let's put a new test pipeline*/
    public void test030PutPipeline() throws JsonProcessingException {
        PipelineDef e = new PipelineDef().setDomain("test").addEngine(
                new PipelineEngine("sample.engine:1"));
        e.setName("test");
        e.setVersion("1");
        String testPipelineDef = FileBased.writeAsString(e);

        given().content(testPipelineDef)//
                .when().put(API_URL)//
                .then().statusCode(STATUS_OK);
    }

    @Test
    /** Putting a faulty pipeline should fail */
    public void test031PutFaultyPipeline() throws JsonProcessingException {
        given().content("blah")//
                .when().put(API_URL)//
                .then().statusCode(STATUS_INVALID);
    }

    @Test
    /** Putting a faulty pipeline should fail (id is missing) */
    public void test032PutFaultyPipeline() throws JsonProcessingException {
        given().content("{  \"name\" : \"opennlp.ners.en\"}")//
                .when().put(API_URL)//
                .then().statusCode(STATUS_INVALID);
    }

    @Test
    /** .. and check that the new test pipeline is here */
    public void test040GetTestPipeline() {
        get(API_URL + "/test/1").then()//
                .contentType(JSON).statusCode(STATUS_OK)//
                .body("name", equalTo("test"))//
                .body("version", equalTo("1"))//
                .body("engines[0].id", equalTo("sample.engine:1"))//
                .body("engines[0].script", equalTo(null))//
        ;
    }

    @Test
    /** .. and check that a bogus pipelien is NOT here */
    public void test041GetFaultyTestPipeline() {
        get(API_URL + "/test/1000000000198198")//
                .then().statusCode(STATUS_INVALID);
    }

    @Test
    /** Now let's delete the test pipeline*/
    public void test050DeletePipeline() throws JsonProcessingException {
        when().delete(API_URL + "/test/1").//
                then().statusCode(STATUS_OK);
    }

    @Test
    /** ... and check that it is gone. */
    public void test060GetTestPipelineGone() {
        get(API_URL + "/test/1")//
                .then().statusCode(STATUS_INVALID);
    }
}
