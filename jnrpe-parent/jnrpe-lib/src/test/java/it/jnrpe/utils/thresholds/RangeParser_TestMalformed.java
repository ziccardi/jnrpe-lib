package it.jnrpe.utils.thresholds;

import org.testng.Assert;
import org.testng.annotations.Test;

public class RangeParser_TestMalformed {

    @Test( expectedExceptions=InvalidRangeSyntaxException.class)
    public void testExclusiveNegativeInfinite() throws Exception {
        RangeStringParser parser = new RangeStringParser();

        RangeConfig rc = new RangeConfig();

        parser.parse("(-inf..+inf", rc);
    }

    @Test( expectedExceptions=InvalidRangeSyntaxException.class)
    public void testExclusivePositiveInfinite() throws Exception {
        RangeStringParser parser = new RangeStringParser();

        RangeConfig rc = new RangeConfig();

        parser.parse("-inf..+inf)", rc);
    }

    @Test( expectedExceptions=RangeException.class)
    public void testLeftBoundaryGreaterThanRightBoundary() throws Exception {
        RangeStringParser parser = new RangeStringParser();

        RangeConfig rc = new RangeConfig();

        parser.parse("50..10", rc);
    }

    @Test( expectedExceptions=InvalidRangeSyntaxException.class)
    public void testLeftBoundaryGreaterThanRightBoundary_infinity() throws Exception {
        RangeStringParser parser = new RangeStringParser();

        RangeConfig rc = new RangeConfig();

        parser.parse("+inf..-inf", rc);
    }

    @Test (expectedExceptions=PrematureEndOfRangeException.class)
    public void testRightIncomplete() throws Exception {
        RangeStringParser parser = new RangeStringParser();

        RangeConfig rc = new RangeConfig();

        parser.parse("inf..", rc);
    }

    @Test (expectedExceptions=InvalidRangeSyntaxException.class)
    public void testLeftIncomplete() throws Exception {
        RangeStringParser parser = new RangeStringParser();

        RangeConfig rc = new RangeConfig();

        parser.parse("..inf", rc);
    }

    @Test (expectedExceptions=RangeException.class)
    public void testLeftDoubleSign() throws Exception {
        RangeStringParser parser = new RangeStringParser();

        RangeConfig rc = new RangeConfig();

        parser.parse("--10..+50", rc);
    }

    @Test (expectedExceptions=RangeException.class)
    public void testRightDoubleSign() throws Exception {
        RangeStringParser parser = new RangeStringParser();

        RangeConfig rc = new RangeConfig();

        parser.parse("10..++50", rc);
    }

    @Test (expectedExceptions=InvalidRangeSyntaxException.class)
    public void testBadLeftBoundary() throws Exception {
        RangeStringParser parser = new RangeStringParser();

        RangeConfig rc = new RangeConfig();

        parser.parse("1a0..+50", rc);
    }

    @Test (expectedExceptions=InvalidRangeSyntaxException.class)
    public void testBadRightBoundary() throws Exception {
        RangeStringParser parser = new RangeStringParser();

        RangeConfig rc = new RangeConfig();

        parser.parse("10..+5a0", rc);
    }
}
