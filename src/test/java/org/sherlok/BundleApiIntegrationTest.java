package org.sherlok;

import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.sherlok.FileBased.writeAsString;
import static org.sherlok.SherlokServer.BUNDLES;
import static org.sherlok.SherlokServer.STATUS_INVALID;
import static org.sherlok.SherlokServer.STATUS_OK;
import static org.sherlok.mappings.BundleDef.BundleDependency.DependencyType.mvn;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
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

    @BeforeClass
    public static void beforeClass() throws Exception {
        SherlokServer.init(TEST_PORT);
    }

    @AfterClass
    public static void afterClass() {
        StopServer.stop();
    }

    @Test
    public void test010GetBundles() {
        get(API_URL).then().contentType(JSON).statusCode(STATUS_OK)
                .content(containsString("\"name\" : \"dkpro_opennlp_en\","));
    }

    @Test
    public void test020GetBundle() {
        get(API_URL + "/dkpro_opennlp_en/1.6.2")
                .then()
                .contentType(JSON)
                .statusCode(STATUS_OK)
                .body("name", equalTo("dkpro_opennlp_en"))
                .body("version", equalTo("1.6.2"))
                .body("repositories.dkpro",
                        equalTo("http://zoidberg.ukp.informatik.tu-darmstadt.de/artifactory/public-model-releases-local/"));
    }

    @Test
    /** Let's put a new test bundle*/
    public void test030PutBundle() throws JsonProcessingException {
        BundleDef e = new BundleDef()
                .addDependency(new BundleDependency(
                        mvn,
                        "de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.stanfordnlp-gpl:1.6.2"));
        e.setName("test");
        e.setVersion("172");

        given().content(writeAsString(e))//
                .when().put(API_URL)//
                .then().statusCode(STATUS_OK);
    }

    @Test
    /** Putting a faulty bundle should fail */
    public void test031PutFaultyBundle() throws JsonProcessingException {
        given().content("blah")//
                .when().put(API_URL)//
                .then().statusCode(STATUS_INVALID);
    }

    @Test
    /** Putting a faulty bundle should fail (id is missing) */
    public void test032PutFaultyBundle() throws JsonProcessingException {
        given().content("{  \"name\" : \"blabla\"}")//
                .when().put(API_URL)//
                .then().statusCode(STATUS_INVALID);
    }

    @Test
    /** .. and check that the new test bundle is here */
    public void test040GetTestBundle() {
        get(API_URL + "/test/172")
                .then()
                .contentType(JSON)
                .statusCode(STATUS_OK)
                .body("name", equalTo("test"))
                .body("version", equalTo("172"))
                .body("dependencies[0].type", equalTo("mvn"))
                .body("dependencies[0].value",
                        equalTo("de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.stanfordnlp-gpl:1.6.2"));
    }

    @Test
    /** .. and check that a bogus bundle is NOT here */
    public void test041GetFaultyTestBundle() {
        get(API_URL + "/test/1000000000198198")//
                .then().statusCode(STATUS_INVALID);
    }

    @Test
    /** Now let's delete the test bundle */
    public void test050DeleteBundle() throws JsonProcessingException {
        when().delete(API_URL + "/test/172").//
                then().statusCode(STATUS_OK);
    }

    @Test
    /** ... and check that it is gone. */
    public void test060GetTestBundleGone() {
        get(API_URL + "/test/172")//
                .then().statusCode(STATUS_INVALID);
    }
}
