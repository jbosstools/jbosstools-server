package org.jboss.ide.eclipse.as.test;

import org.jboss.ide.eclipse.as.classpath.test.JBIDE1657Test;
import org.jboss.ide.eclipse.as.classpath.test.JEEClasspathContainerTest;
import org.jboss.ide.eclipse.as.test.model.RuntimeServerModelTest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ASTestSuite extends TestSuite {
    public static Test suite() { 
        TestSuite suite = new TestSuite("ASTools Test Suite");

        suite.addTestSuite(RuntimeServerModelTest.class);
        suite.addTestSuite(JEEClasspathContainerTest.class);
        suite.addTestSuite(JBIDE1657Test.class);
        return suite; 
   }

}
