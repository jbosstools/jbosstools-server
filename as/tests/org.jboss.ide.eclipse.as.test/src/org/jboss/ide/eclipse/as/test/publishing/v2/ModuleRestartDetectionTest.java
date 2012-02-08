/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.test.publishing.v2;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServerBehavior;
import org.jboss.ide.eclipse.as.test.publishing.v2.ModuleRestartDetectionTest.MockFile;

public class ModuleRestartDetectionTest extends TestCase {

	static class MockFile implements IModuleFile {

		String name;
		
		public MockFile(String name) {
			this.name = name;
		}
		
		public IPath getModuleRelativePath() {
			return null;
		}

		public String getName() {
			return name;
		}

		public Object getAdapter(Class adapter) {
			return null;
		}

		public long getModificationStamp() {
			return 0;
		}
		
	}

	private DeployableServerBehavior behavior;
	private MockFile mixedJar;
	private MockFile classyjar;
	private MockFile htmlfile;
	private MockFile classfile;
	private MockFile jarfile;
	private MockFile nodotjar;
	
	public void testDefaults() {
		
		
		assertTrue("Default behavior should restart on .jar", behavior.changedFileRequiresModuleRestart(jarfile));
		assertFalse("Default behavior should not restart on .class", behavior.changedFileRequiresModuleRestart(classfile));
		assertFalse("Default behavior should not restart on .html", behavior.changedFileRequiresModuleRestart(htmlfile));
		assertFalse("Default should not restart on file named classy.jarfile", behavior.changedFileRequiresModuleRestart(classyjar));
		assertFalse("Default should not restart on nodotjar", behavior.changedFileRequiresModuleRestart(nodotjar));
		
		assertTrue("default behavior should restart on JAr too", behavior.changedFileRequiresModuleRestart(mixedJar));
		
	}

	
	public void setUp() {
		behavior = new DeployableServerBehavior();
		
		jarfile = new MockFile("blah.jar");
		classfile = new MockFile("blah.class");
		nodotjar = new MockFile("nodotjar");
		htmlfile = new MockFile("blah.html");
		classyjar = new MockFile("myclassy.jarfile");
		mixedJar = new MockFile("BLAH.JAr");
	}
	
	public void testClassEndOfLine() {
		
		String filepattern = ".class$";
		behavior.setRestartFilePattern(filepattern);
		
		assertFalse(filepattern + " should not restart on .jar", behavior.changedFileRequiresModuleRestart(jarfile));
		assertTrue(filepattern + " should restart on .class", behavior.changedFileRequiresModuleRestart(classfile));
		assertFalse(filepattern + " should not restart on .html", behavior.changedFileRequiresModuleRestart(htmlfile));
		assertFalse(filepattern + "Default should not restart on file named classy.jarfile", behavior.changedFileRequiresModuleRestart(classyjar));
	}
	
	public void testBasicOr() {
		String filepattern = ".class|.jar";
		behavior.setRestartFilePattern(filepattern);
		
		assertTrue(filepattern + " should restart on .jar", behavior.changedFileRequiresModuleRestart(jarfile));
		assertTrue(filepattern + " should restart on .class", behavior.changedFileRequiresModuleRestart(classfile));
		assertFalse(filepattern + " should not restart on .html", behavior.changedFileRequiresModuleRestart(htmlfile));
		assertTrue(filepattern + " should restart on file named classy.jarfile", behavior.changedFileRequiresModuleRestart(classyjar));
		assertTrue(filepattern + " behavior should restart on JAr too", behavior.changedFileRequiresModuleRestart(mixedJar));
	}
	
	public void testCaseInsensitive() {
	
		assertTrue(behavior.changedFileRequiresModuleRestart(new MockFile(".jar")));

		assertTrue(behavior.changedFileRequiresModuleRestart(new MockFile(".JAR")));

		behavior.setRestartFilePattern(".jar");
		
		assertTrue(behavior.changedFileRequiresModuleRestart(new MockFile(".jar")));

		assertTrue(behavior.changedFileRequiresModuleRestart(new MockFile(".JAR")));

	}
	
	public void testBasicOrEndOfLine() {
		String filepattern = ".class$|.jar$";
		behavior.setRestartFilePattern(filepattern);
		
		assertTrue(filepattern + " should restart on .jar", behavior.changedFileRequiresModuleRestart(jarfile));
		assertTrue(filepattern + " should restart on .class", behavior.changedFileRequiresModuleRestart(classfile));
		assertFalse(filepattern + " should not restart on .html", behavior.changedFileRequiresModuleRestart(htmlfile));
		assertFalse(filepattern + "Default should not restart on file named classy.jarfile", behavior.changedFileRequiresModuleRestart(classyjar));
		
	}
	
	public void testSystemProperty() {
		
		System.setProperty(DeployableServerBehavior.ORG_JBOSS_TOOLS_AS_RESTART_FILE_PATTERN,".blah");
		
		behavior = new DeployableServerBehavior();
		
		assertTrue(behavior.changedFileRequiresModuleRestart(new MockFile(".blah")));
		assertFalse(behavior.changedFileRequiresModuleRestart(new MockFile(".jar")));
		
	}
}
