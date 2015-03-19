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

import static org.sherlok.utils.CheckThat.checkOnlyAlphanumDotUnderscore;
import static org.sherlok.utils.CheckThat.validateDomain;
import static org.sherlok.utils.CheckThat.validateId;

import org.junit.Test;
import org.sherlok.utils.CheckThat;
import org.sherlok.utils.ValidationException;

public class CheckThatTest {

    @Test(expected = ValidationException.class)
    public void testNull() throws Exception {
        checkOnlyAlphanumDotUnderscore(null, "");
    }

    @Test(expected = ValidationException.class)
    public void testEmpty() throws Exception {
        checkOnlyAlphanumDotUnderscore("", "");
    }

    @Test(expected = ValidationException.class)
    public void testStar() throws Exception {
        checkOnlyAlphanumDotUnderscore("*_", "");
    }

    @Test(expected = ValidationException.class)
    public void testParenthesis() throws Exception {
        checkOnlyAlphanumDotUnderscore("(asd)", "");
    }

    @Test
    public void test() throws Exception {
        checkOnlyAlphanumDotUnderscore("abAC09.32no__in23", "");
    }

    // DOMAIN

    @Test(expected = ValidationException.class)
    public void testDomainNull() throws Exception {
        validateDomain(null, "");
    }

    @Test
    public void testDomainEmpty() throws Exception {
        validateDomain("", "");// this is OK!
    }

    @Test(expected = ValidationException.class)
    public void testDomainStar() throws Exception {
        validateDomain("*_", "");
    }

    @Test(expected = ValidationException.class)
    public void testDomainDotDot() throws Exception {
        validateDomain("a..b", "");
    }

    @Test(expected = ValidationException.class)
    public void testDomainParenthesis() throws Exception {
        validateDomain("(asd)", "");
    }

    @Test
    public void testDomain() throws Exception {
        validateDomain("abAC/09.32/no__in23", "");
    }

    // ID

    @Test
    public void testValidId() throws Exception {
        validateId("ab:cd", "");
    }

    @Test
    public void testValidIdWithUnderscores() throws Exception {
        validateId("a_b:c_d", "");
    }

    @Test(expected = ValidationException.class)
    public void testValidIdTwoColumns() throws Exception {
        validateId("a:_b:c_d", "");
    }

    @Test(expected = ValidationException.class)
    public void testValidIdEmpty() throws Exception {
        validateId("ab:", "");
    }

    @Test(expected = ValidationException.class)
    public void testValidIdMissingColumn() throws Exception {
        validateId("a_bc_d", "");
    }

    @Test
    public void testJavaIdentifier() throws Exception {
        CheckThat.validateJavaIdentifier("C", "errr1");
        CheckThat.validateJavaIdentifier("Cc", "errr2");
        CheckThat.validateJavaIdentifier("b.C", "errr3");
        CheckThat.validateJavaIdentifier("b.Cc", "errr4");
        CheckThat.validateJavaIdentifier("aAa.b.Cc", "errr5");
        CheckThat.validateJavaIdentifier("a.b.Cc", "errr6");

        // after the initial character identifiers may use any combination of
        // letters and digits, underscores or dollar signs
        CheckThat.validateJavaIdentifier("a.b.C_c", "errr10");
        CheckThat.validateJavaIdentifier("a.b.C$c", "errr11");
        CheckThat.validateJavaIdentifier("a.b.C9", "errr12");
    }

    @Test(expected = ValidationException.class)
    public void testJavaIdentifierFailDot() throws Exception {
        CheckThat.validateJavaIdentifier(".C", "cannot start with a dot");
    }

    @Test(expected = ValidationException.class)
    public void testJavaIdentifierFailDot2() throws Exception {
        CheckThat.validateJavaIdentifier("C.", "cannot end with a dot");
    }

    @Test(expected = ValidationException.class)
    public void testJavaIdentifierFailDotDot() throws Exception {
        CheckThat.validateJavaIdentifier("b..C",
                "cannot have two dots following each other");
    }

    @Test(expected = ValidationException.class)
    public void testJavaIdentifierFailNumber() throws Exception {
        CheckThat.validateJavaIdentifier("b.9C", "cannot start with a number");
    }

    @Test
    public void testTypeIdentifier() throws Exception {
        CheckThat.validateTypeIdentifier("Cc", "e1");
        CheckThat.validateTypeIdentifier("b.C", "e2");
        CheckThat.validateTypeIdentifier("aAa.b.Cc", "e3");
        CheckThat.validateTypeIdentifier("Cc.*", "e4");
        CheckThat.validateTypeIdentifier("aAa.b.Cc.*", "e5");
    }

    @Test(expected = ValidationException.class)
    public void testTypeIdentifierFail() throws Exception {
        CheckThat.validateJavaIdentifier("aAa.b.Cc.", "cannot end with dot");
    }

    @Test(expected = ValidationException.class)
    public void testTypeIdentifierFail2() throws Exception {
        CheckThat
                .validateJavaIdentifier("aAa.b.Cc*", "cannot end with asterix");
    }
}
