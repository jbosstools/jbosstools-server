/**
 * JBoss by Red Hat
 * Copyright 2006-2009, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.ide.eclipse.as.test.classpath.JBIDE1657Test;
import org.jboss.ide.eclipse.as.test.classpath.JEEClasspathContainerTest;
import org.jboss.ide.eclipse.as.test.classpath.ProjectRuntimeTest;
import org.jboss.ide.eclipse.as.test.classpath.RuntimeServerModelTest;
import org.jboss.ide.eclipse.as.test.projectcreation.TestEar5WithJBossRuntime;
import org.jboss.ide.eclipse.as.test.publishing.JBIDE2512aTest;
import org.jboss.ide.eclipse.as.test.publishing.JBIDE2512bTest;
import org.jboss.ide.eclipse.as.test.publishing.JBIDE4184Test;
import org.jboss.ide.eclipse.as.test.publishing.v2.JSTDeploymentTester;
import org.jboss.ide.eclipse.as.test.publishing.v2.SingleFileDeploymentTester;

public class ASTestSuite extends TestSuite {
    public static Test suite() { 
        TestSuite suite = new TestSuite("ASTools Test Suite");
        suite.addTestSuite(PreReqTest.class);
        suite.addTestSuite(RuntimeServerModelTest.class);
        suite.addTestSuite(JEEClasspathContainerTest.class);
        suite.addTestSuite(ProjectRuntimeTest.class);
        suite.addTestSuite(JBIDE1657Test.class);
        suite.addTestSuite(JBIDE2512aTest.class);
        suite.addTestSuite(JBIDE2512bTest.class);
        suite.addTestSuite(JBIDE4184Test.class);
        suite.addTestSuite(TestEar5WithJBossRuntime.class);
        suite.addTestSuite(JSTDeploymentTester.class);
        suite.addTestSuite(SingleFileDeploymentTester.class);
        return suite; 
   }

}
