/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.archives.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.ide.eclipse.archives.test.core.ArchivesCoreTest;
import org.jboss.ide.eclipse.archives.test.core.ant.SimpleAntTest;
import org.jboss.ide.eclipse.archives.test.model.DirectoryScannerTest;
import org.jboss.ide.eclipse.archives.test.model.FilesetMatchesPathTest;
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
import org.jboss.ide.eclipse.archives.test.ui.BuildActionTest;
import org.jboss.ide.eclipse.archives.test.util.TruezipUtilTest;

public class ArchivesTestSuite extends TestSuite {
    public static Test suite() { 
        TestSuite suite = new TestSuite("Archives Tests");
        suite.addTestSuite(ArchivesCoreTest.class);
        suite.addTestSuite(XBMarshallTest.class);
        suite.addTestSuite(XBUnmarshallTest.class);
        suite.addTestSuite(TruezipUtilTest.class);
        suite.addTestSuite(ModelUtilTest.class);
        suite.addTestSuite(DirectoryScannerTest.class);
        suite.addTestSuite(ModelCreationTest.class);
        suite.addTestSuite(ModelTruezipBridgeTest.class);
        suite.addTestSuite(SimpleAntTest.class);
        suite.addTestSuite(FilesetMatchesPathTest.class);
        
        // jiras
        suite.addTestSuite(JBIDE1406Test.class);
        suite.addTestSuite(JBIDE2099Test.class);
        suite.addTestSuite(JBIDE2296Test.class);
        suite.addTestSuite(JBIDE2311Test.class);
        suite.addTestSuite(JBIDE2315Test.class);
        suite.addTestSuite(JBIDE2439Test.class);
        
        
        // UI
        suite.addTestSuite(BuildActionTest.class);
        return suite; 
   }

}
