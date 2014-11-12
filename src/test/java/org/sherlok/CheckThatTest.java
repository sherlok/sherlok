package org.sherlok;

import org.junit.Test;
import org.sherlok.mappings.ValidationException;

public class CheckThatTest {

    @Test(expected = ValidationException.class)
    public void testStar() {
        CheckThat.isOnlyAlphanumDotUnderscore("*_");
    }

    @Test(expected = ValidationException.class)
    public void testParenth() {
        CheckThat.isOnlyAlphanumDotUnderscore("(asd)");
    }

    @Test(expected = ValidationException.class)
    public void testParenth2() {
        CheckThat.checkOnlyAlphanumUnderscore("(asd)");
    }

    @Test
    public void test() {
        CheckThat.isOnlyAlphanumDotUnderscore("abAC09.32no__in23");
    }

    @Test(expected = ValidationException.class)
    public void testDot() {
        CheckThat.checkOnlyAlphanumUnderscore("abAC09.32no__in23");
    }

    @Test
    public void testUnderscore() {
        CheckThat.checkOnlyAlphanumUnderscore("abAC0adfsweMNOINW932no__in23");
    }

    @Test
    public void testValidId() {
        CheckThat.isValidId("ab:cd");
    }

    @Test
    public void testValidId2() {
        CheckThat.isValidId("a_b:c_d");
    }

    @Test(expected = ValidationException.class)
    public void testValidId3() {
        CheckThat.isValidId("a:_b:c_d");
    }

    @Test(expected = ValidationException.class)
    public void testValidId4() {
        CheckThat.isValidId("a_bc_d");
    }
}
