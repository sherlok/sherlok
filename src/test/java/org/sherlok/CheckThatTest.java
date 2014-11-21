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

import static org.sherlok.utils.CheckThat.checkValidId;

import org.junit.Test;
import org.sherlok.utils.CheckThat;
import org.sherlok.utils.ValidationException;

public class CheckThatTest {

    @Test(expected = ValidationException.class)
    public void testStar() throws Exception {
        CheckThat.checkOnlyAlphanumDot("*_");
    }

    @Test(expected = ValidationException.class)
    public void testParenthesis() throws Exception {
        CheckThat.checkOnlyAlphanumDot("(asd)");
    }

    @Test
    public void test() throws Exception {
        CheckThat.checkOnlyAlphanumDot("abAC09.32no__in23");
    }

    @Test
    public void testValidId() throws Exception {
        checkValidId("ab:cd");
    }

    @Test
    public void testValidIdWithUnderscores() throws Exception {
        checkValidId("a_b:c_d");
    }

    @Test(expected = ValidationException.class)
    public void testValidIdTwoColumns() throws Exception {
        checkValidId("a:_b:c_d");
    }

    @Test(expected = ValidationException.class)
    public void testValidIdMissingColumn() throws Exception {
        checkValidId("a_bc_d");
    }
}
