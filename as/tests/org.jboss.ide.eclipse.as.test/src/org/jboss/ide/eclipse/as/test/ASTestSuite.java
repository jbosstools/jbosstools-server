package org.jboss.ide.eclipse.as.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ASTestSuite extends TestSuite {
    public static Test suite() { 
        TestSuite suite = new TestSuite("Archives Tests");

        suite.addTestSuite(ASClasspathTest.class);

        return suite; 
   }

}
