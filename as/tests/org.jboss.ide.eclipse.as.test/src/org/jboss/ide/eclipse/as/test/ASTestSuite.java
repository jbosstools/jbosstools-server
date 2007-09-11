package org.jboss.ide.eclipse.as.test;

import org.jboss.ide.eclipse.as.classpath.test.JEEClasspathContainerTest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ASTestSuite extends TestSuite {
    public static Test suite() { 
        TestSuite suite = new TestSuite("Archives Tests");

        suite.addTestSuite(ASClasspathTest.class);
        suite.addTestSuite(JEEClasspathContainerTest.class);
        return suite; 
   }

}
