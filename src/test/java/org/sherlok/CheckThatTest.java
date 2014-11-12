package org.sherlok;

import static org.sherlok.utils.CheckThat.checkValidId;

import org.junit.Test;
import org.sherlok.utils.CheckThat;
import org.sherlok.utils.ValidationException;

public class CheckThatTest {

    @Test(expected = ValidationException.class)
    public void testStar() {
        CheckThat.checkOnlyAlphanumDotUnderscore("*_");
    }

    @Test(expected = ValidationException.class)
    public void testParenthesis() {
        CheckThat.checkOnlyAlphanumDotUnderscore("(asd)");
    }

    @Test(expected = ValidationException.class)
    public void testParenthesis2() {
        CheckThat.checkOnlyAlphanumUnderscore("(asd)");
    }

    @Test
    public void test() {
        CheckThat.checkOnlyAlphanumDotUnderscore("abAC09.32no__in23");
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
        checkValidId("ab:cd");
    }

    @Test
    public void testValidIdWithUnderscores() {
        checkValidId("a_b:c_d");
    }

    @Test(expected = ValidationException.class)
    public void testValidIdTwoColumns() {
        checkValidId("a:_b:c_d");
    }

    @Test(expected = ValidationException.class)
    public void testValidIdMissingColumn() {
        checkValidId("a_bc_d");
    }
}
