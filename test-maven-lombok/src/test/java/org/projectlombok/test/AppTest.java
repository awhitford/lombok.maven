package org.projectlombok.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName ) {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() {
        final DataExample dex1 = new DataExample("One");
        dex1.setScore(1);
        final DataExample dex2 = new DataExample("Two");
        dex2.setScore(2);

        assertTrue( !dex1.equals(dex2) );
    }
}
