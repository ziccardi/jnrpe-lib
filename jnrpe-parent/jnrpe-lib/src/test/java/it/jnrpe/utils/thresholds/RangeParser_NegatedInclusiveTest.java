package it.jnrpe.utils.thresholds;

import java.math.BigDecimal;

import org.testng.Assert;
import org.testng.annotations.Test;

public class RangeParser_NegatedInclusiveTest {

    @Test
    public void testNegatedInclusiveOk() throws RangeException {
        RangeStringParser parser = new RangeStringParser();

        RangeConfig rc = new RangeConfig();

        parser.parse("^10.23..5000.1", rc);

        Assert.assertTrue(rc.isLeftInclusive());
        Assert.assertTrue(rc.isRightInclusive());
        Assert.assertTrue(rc.isNegate());
        Assert.assertEquals(rc.getLeftBoundary(), new BigDecimal("10.23"));
        Assert.assertEquals(rc.getRightBoundary(), new BigDecimal("5000.1"));
        Assert.assertFalse(rc.isNegativeInfinity());
        Assert.assertFalse(rc.isPositiveInfinity());
    }

    @Test
    public void testNegatedInclusiveLeftOk() throws RangeException {
        RangeStringParser parser = new RangeStringParser();

        RangeConfig rc = new RangeConfig();

        parser.parse("^10.23..5000.1)", rc);

        Assert.assertTrue(rc.isLeftInclusive());
        Assert.assertFalse(rc.isRightInclusive());
        Assert.assertTrue(rc.isNegate());
        Assert.assertEquals(rc.getLeftBoundary(), new BigDecimal("10.23"));
        Assert.assertEquals(rc.getRightBoundary(), new BigDecimal("5000.1"));
        Assert.assertFalse(rc.isNegativeInfinity());
        Assert.assertFalse(rc.isPositiveInfinity());
    }

    @Test
    public void testNegatedInclusiveRightOk() throws RangeException {
        RangeStringParser parser = new RangeStringParser();

        RangeConfig rc = new RangeConfig();

        parser.parse("^(10.23..5000.1", rc);

        Assert.assertFalse(rc.isLeftInclusive());
        Assert.assertTrue(rc.isRightInclusive());
        Assert.assertTrue(rc.isNegate());
        Assert.assertEquals(rc.getLeftBoundary(), new BigDecimal("10.23"));
        Assert.assertEquals(rc.getRightBoundary(), new BigDecimal("5000.1"));
        Assert.assertFalse(rc.isNegativeInfinity());
        Assert.assertFalse(rc.isPositiveInfinity());
    }

    @Test
    public void testNegatedExclusiveBothOk() throws RangeException {
        RangeStringParser parser = new RangeStringParser();

        RangeConfig rc = new RangeConfig();

        parser.parse("^(10.23..5000.1)", rc);

        Assert.assertFalse(rc.isLeftInclusive());
        Assert.assertFalse(rc.isRightInclusive());
        Assert.assertTrue(rc.isNegate());
        Assert.assertEquals(rc.getLeftBoundary(), new BigDecimal("10.23"));
        Assert.assertEquals(rc.getRightBoundary(), new BigDecimal("5000.1"));
        Assert.assertFalse(rc.isNegativeInfinity());
        Assert.assertFalse(rc.isPositiveInfinity());
    }
}
