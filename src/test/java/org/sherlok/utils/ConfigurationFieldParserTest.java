package org.sherlok.utils;

import static org.junit.Assert.*;

import java.lang.reflect.Field;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.junit.Test;

public class ConfigurationFieldParserTest {

    @ConfigurationParameter(defaultValue = "2,3,4")
    public int[] intArray;

    @ConfigurationParameter(defaultValue = "2,3,4")
    public String string;

    @ConfigurationParameter(defaultValue = "true, false")
    public boolean[] booleanArray;

    @Test
    public void test() throws Exception {

        String[] stringArray = { "1", "2" };
        Field field = ConfigurationFieldParserTest.class.getField("intArray");
        Object o = ConfigurationFieldParser.getDefaultValue(field, stringArray);
        assertTrue(o.getClass().isArray());
        assertTrue(o instanceof Integer[]);

        String[] stringArraySingle = { "hello" };
        field = ConfigurationFieldParserTest.class.getField("string");
        o = ConfigurationFieldParser.getDefaultValue(field, stringArraySingle);
        assertFalse(o.getClass().isArray());
        assertTrue(o instanceof String);

        String[] stringArrayBoolean = { "true", "false" };
        field = ConfigurationFieldParserTest.class.getField("booleanArray");
        o = ConfigurationFieldParser.getDefaultValue(field, stringArrayBoolean);
        assertTrue(o.getClass().isArray());
        assertTrue(o instanceof Boolean[]);
    }

}
