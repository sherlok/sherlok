package org.sherlok;

import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.sherlok.SherlokServer.ENGINES;
import static org.sherlok.SherlokServer.STATUS_INVALID;
import static org.sherlok.SherlokServer.STATUS_OK;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.sherlok.mappings.EngineDef;

import spark.StopServer;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Integration tests for engine REST API. This runs in a separate Spark server
 * on another port. However, ATM it share the same config, so stuff created
 * should be deleted after a test.
 * 
 * @author renaud@apache.org
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EngineApiIntegrationTest {

    static final int TEST_PORT = 9607;
    static final String API_URL = "http://localhost:" + TEST_PORT + "/"
            + ENGINES;

    @BeforeClass
    public static void beforeClass() throws Exception {
        SherlokServer.init(TEST_PORT);
    }

    @AfterClass
    public static void afterClass() {
        StopServer.stop();
    }

    @Test
    public void test010GetEngines() {
        get(API_URL)
                .then()
                .contentType(JSON)
                .statusCode(STATUS_OK)
                .content(containsString("opennlp.ner.person.en"))
                .content(
                        containsString("\"class\" : \"de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpNameFinder\","));
    }

    @Test
    public void test020GetEngine() {
        get(API_URL + "/opennlp.ner.person.en/1.6.2").then()//
                .contentType(JSON).statusCode(STATUS_OK)//
                .body("name", equalTo("opennlp.ner.person.en"))//
                .body("version", equalTo("1.6.2"))//
                .body("domain", equalTo("dkpro"))//
                .body("parameters.modelVariant", equalTo("person"));
    }

    @Test
    /** Let's put a new test engine*/
    public void test030PutEngine() throws JsonProcessingException {
        EngineDef e = new EngineDef()
                .setDomain("test")
                .setBundleId("dkpro.opennlp.en:1.6.2")
                .setClassz(
                        "de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter");
        e.setName("test");
        e.setVersion("17");

        given().content(FileBased.writeAsString(e))//
                .when().put(API_URL)//
                .then().statusCode(STATUS_OK);
    }

    @Test
    /** Putting a faulty engine should fail */
    public void test031PutFaultyEngine() throws JsonProcessingException {
        given().content("blah")//
                .when().put(API_URL)//
                .then().statusCode(STATUS_INVALID);
    }

    @Test
    /** Putting a faulty engine should fail (id is missing) */
    public void test032PutFaultyEngine() throws JsonProcessingException {
        given().content("{  \"name\" : \"blabla\"}")//
                .when().put(API_URL)//
                .then().statusCode(STATUS_INVALID);
    }

    @Test
    /** .. and check that the new test engine is here */
    public void test040GetTestEngine() {
        get(API_URL + "/test/17")
                .then()
                .contentType(JSON)
                .statusCode(STATUS_OK)
                .body("name", equalTo("test"))
                .body("version", equalTo("17"))
                .body("class",
                        equalTo("de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter"))
                .body("script", equalTo(null));
    }

    @Test
    /** .. and check that a bogus engine is NOT here */
    public void test041GetFaultyTestEngine() {
        get(API_URL + "/test/1000000000198198")//
                .then().statusCode(STATUS_INVALID);
    }

    @Test
    /** Now let's delete the test engine */
    public void test050DeleteEngine() throws JsonProcessingException {
        when().delete(API_URL + "/test/17").//
                then().statusCode(STATUS_OK);
    }

    @Test
    /** ... and check that it is gone. */
    public void test060GetTestEngineGone() {
        get(API_URL + "/test/17")//
                .then().statusCode(STATUS_INVALID);
    }
}
