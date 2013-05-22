package it.jnrpe.utils.thresholds;

import java.math.BigDecimal;

import org.testng.Assert;
import org.testng.annotations.Test;

public class RangeParser_InfinityTest {

    @Test
    public void testImplicitNegativeInfinity() throws RangeException {
        RangeStringParser parser = new RangeStringParser();

        RangeConfig rc = new RangeConfig();

        parser.parse("inf..5000.1", rc);

        Assert.assertTrue(rc.isLeftInclusive());
        Assert.assertTrue(rc.isRightInclusive());
        Assert.assertFalse(rc.isNegate());
        Assert.assertNull(rc.getLeftBoundary());
        Assert.assertEquals(rc.getRightBoundary(), new BigDecimal("5000.1"));
        Assert.assertTrue(rc.isNegativeInfinity());
        Assert.assertFalse(rc.isPositiveInfinity());
    }

    @Test
    public void testExlicitNegativeInfinity() throws RangeException {
        RangeStringParser parser = new RangeStringParser();

        RangeConfig rc = new RangeConfig();

        parser.parse("-inf..5000.1", rc);

        Assert.assertTrue(rc.isLeftInclusive());
        Assert.assertTrue(rc.isRightInclusive());
        Assert.assertFalse(rc.isNegate());
        Assert.assertNull(rc.getLeftBoundary());
        Assert.assertEquals(rc.getRightBoundary(), new BigDecimal("5000.1"));
        Assert.assertTrue(rc.isNegativeInfinity());
        Assert.assertFalse(rc.isPositiveInfinity());
    }

    @Test
    public void testImplicitPositiveInfinity() throws RangeException {
        RangeStringParser parser = new RangeStringParser();

        RangeConfig rc = new RangeConfig();

        parser.parse("50..inf", rc);

        Assert.assertTrue(rc.isLeftInclusive());
        Assert.assertTrue(rc.isRightInclusive());
        Assert.assertFalse(rc.isNegate());
        Assert.assertEquals(rc.getLeftBoundary(), new BigDecimal("50"));
        Assert.assertNull(rc.getRightBoundary());
        Assert.assertFalse(rc.isNegativeInfinity());
        Assert.assertTrue(rc.isPositiveInfinity());
    }

    @Test
    public void testExplicitPositiveInfinity() throws RangeException {
        RangeStringParser parser = new RangeStringParser();

        RangeConfig rc = new RangeConfig();

        parser.parse("50..+inf", rc);

        Assert.assertTrue(rc.isLeftInclusive());
        Assert.assertTrue(rc.isRightInclusive());
        Assert.assertFalse(rc.isNegate());
        Assert.assertEquals(rc.getLeftBoundary(), new BigDecimal("50"));
        Assert.assertNull(rc.getRightBoundary());
        Assert.assertFalse(rc.isNegativeInfinity());
        Assert.assertTrue(rc.isPositiveInfinity());
    }

    @Test
    public void testImplicitPositiveNegativeInfinity() throws RangeException {
        RangeStringParser parser = new RangeStringParser();

        RangeConfig rc = new RangeConfig();

        parser.parse("inf..inf", rc);

        Assert.assertTrue(rc.isLeftInclusive());
        Assert.assertTrue(rc.isRightInclusive());
        Assert.assertFalse(rc.isNegate());
        Assert.assertNull(rc.getLeftBoundary());
        Assert.assertNull(rc.getRightBoundary());
        Assert.assertTrue(rc.isNegativeInfinity());
        Assert.assertTrue(rc.isPositiveInfinity());
    }

    @Test
    public void testExplicitPositiveNegativeInfinity() throws RangeException {
        RangeStringParser parser = new RangeStringParser();

        RangeConfig rc = new RangeConfig();

        parser.parse("-inf..+inf", rc);

        Assert.assertTrue(rc.isLeftInclusive());
        Assert.assertTrue(rc.isRightInclusive());
        Assert.assertFalse(rc.isNegate());
        Assert.assertNull(rc.getLeftBoundary());
        Assert.assertNull(rc.getRightBoundary());
        Assert.assertTrue(rc.isNegativeInfinity());
        Assert.assertTrue(rc.isPositiveInfinity());
    }
}
