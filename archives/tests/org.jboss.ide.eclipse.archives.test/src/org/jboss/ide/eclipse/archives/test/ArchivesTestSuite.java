package org.jboss.ide.eclipse.archives.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.ide.eclipse.archives.test.core.ArchivesCoreTest;
import org.jboss.ide.eclipse.archives.test.model.ModelCreationTest;
import org.jboss.ide.eclipse.archives.test.model.ModelTruezipBridgeTest;
import org.jboss.ide.eclipse.archives.test.model.ModelUtilTest;
import org.jboss.ide.eclipse.archives.test.model.XBMarshallTest;
import org.jboss.ide.eclipse.archives.test.model.XBUnmarshallTest;
import org.jboss.ide.eclipse.archives.test.projects.JBIDE1406Test;
import org.jboss.ide.eclipse.archives.test.projects.JBIDE2099Test;
import org.jboss.ide.eclipse.archives.test.projects.JBIDE2296Test;
import org.jboss.ide.eclipse.archives.test.projects.JBIDE2311Test;
import org.jboss.ide.eclipse.archives.test.projects.JBIDE2315Test;
import org.jboss.ide.eclipse.archives.test.projects.JBIDE2439Test;
import org.jboss.ide.eclipse.archives.test.util.TruezipUtilTest;

public class ArchivesTestSuite extends TestSuite {
    public static Test suite() { 
        TestSuite suite = new TestSuite("Archives Tests");
        suite.addTestSuite(ArchivesCoreTest.class);
        suite.addTestSuite(XBMarshallTest.class);
        suite.addTestSuite(XBUnmarshallTest.class);
        suite.addTestSuite(TruezipUtilTest.class);
        suite.addTestSuite(ModelUtilTest.class);
        suite.addTestSuite(ModelCreationTest.class);
        suite.addTestSuite(ModelTruezipBridgeTest.class);
              
        // jiras
        suite.addTestSuite(JBIDE1406Test.class);
        suite.addTestSuite(JBIDE2099Test.class);
        suite.addTestSuite(JBIDE2296Test.class);
        suite.addTestSuite(JBIDE2311Test.class);
        suite.addTestSuite(JBIDE2315Test.class);
        suite.addTestSuite(JBIDE2439Test.class);
        
        return suite; 
   }

}
