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
