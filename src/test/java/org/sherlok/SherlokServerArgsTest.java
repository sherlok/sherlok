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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sherlok.SherlokServer.CliArguments;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class SherlokServerArgsTest {

    @Test
    public void testPort() {

        CliArguments arg = new CliArguments();
        String[] args = { "-port", "12" };
        new JCommander(arg, args);

        assertEquals(12, arg.port);
    }

    @Test
    public void testAddress() {

        CliArguments arg = new CliArguments();
        String[] args = { "-address", "abcd" };
        new JCommander(arg, args);

        assertEquals("abcd", arg.address);
    }

    @Test(expected = ParameterException.class)
    public void testFail() {
        String[] args = { "-port", "woops" };
        new JCommander(new CliArguments(), args);
    }

    @Test(expected = ParameterException.class)
    public void testFail2() {
        String[] args = { "-posdfrt", "sd12" };
        new JCommander(new CliArguments(), args);
    }
}
