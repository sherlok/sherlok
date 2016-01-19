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

import static com.jayway.restassured.RestAssured.delete;
import static org.junit.Assert.assertTrue;
import static org.sherlok.SherlokServer.CLEAN;
import static org.sherlok.SherlokServer.DEFAULT_IP;
import static org.sherlok.SherlokServer.REMOTE_RESOURCES;
import static org.sherlok.SherlokServer.STATUS_OK;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sherlok.SherlokServer;
import org.sherlok.config.GitConfigVariable;
import org.sherlok.config.HttpConfigVariable;
import org.sherlok.config.ProcessConfigVariableException;

import spark.StopServer;

/**
 * Integration tests for cleaning resources REST API.
 */
public class CleanApiIntegrationTest {

    static final int TEST_PORT = 9610;
    static final String CLEAN_API_URL = "http://localhost:" + TEST_PORT + "/"
            + CLEAN + "/" + REMOTE_RESOURCES;

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
    public void testCleanResource() throws ProcessConfigVariableException {
        // First, load some resources
        GitConfigVariable gitVar = new GitConfigVariable(
                "https://github.com/sherlok/sherlok_dependency_test.git", null,
                false);
        HttpConfigVariable httpVar = new HttpConfigVariable(
                "https://raw.githubusercontent.com/sherlok/sherlok_dependency_test/master/resources/file.txt",
                false);
        File gitFile = new File(gitVar.getProcessedValue());
        File httpFile = new File(httpVar.getProcessedValue());

        // Assume those files exists
        assertTrue(gitFile.exists());
        assertTrue(httpFile.exists());
        
        // Delete git only
        delete(CLEAN_API_URL + "/" + "git").then().log().ifError()
                .statusCode(STATUS_OK);
        assertTrue(!gitFile.exists());
        assertTrue(httpFile.exists());

        // Re-download it
        gitFile = new File(gitVar.getProcessedValue());
        assertTrue(gitFile.exists());

        // Delete http only
        delete(CLEAN_API_URL + "/" + "http").then().log().ifError()
                .statusCode(STATUS_OK);
        assertTrue(gitFile.exists());
        assertTrue(!httpFile.exists());

        // Re-download it
        httpFile = new File(httpVar.getProcessedValue());
        assertTrue(httpFile.exists());

        // Clean everything
        delete(CLEAN_API_URL).then().log().ifError().statusCode(STATUS_OK);
        assertTrue(!gitFile.exists());
        assertTrue(!httpFile.exists());
    }
}

