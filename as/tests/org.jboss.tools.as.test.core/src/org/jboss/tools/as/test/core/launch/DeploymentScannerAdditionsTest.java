/******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.as.test.core.launch;

import java.util.Collection;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.internal.AbstractDeploymentScannerAdditions;
import org.jboss.ide.eclipse.as.core.server.internal.JMXServerDeploymentScannerAdditions;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.v7.LocalJBoss7DeploymentScannerAdditions;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.tools.as.test.core.ASMatrixTests;
import org.jboss.tools.as.test.core.internal.utils.ResourceUtils;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;
import org.jboss.tools.as.test.core.internal.utils.wtp.CreateProjectOperationsUtility;
import org.jboss.tools.as.test.core.internal.utils.wtp.JavaEEFacetConstants;
import org.jboss.tools.as.test.core.internal.utils.wtp.OperationTestCase;
import org.jboss.tools.as.test.core.parametized.server.ServerParameterUtils;
import org.jboss.tools.as.test.core.parametized.server.publishing.AbstractPublishingTest;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class DeploymentScannerAdditionsTest extends TestCase  {
	private String serverType;
	
	@Parameters
	public static Collection<Object[]> data() {
		 return ServerParameterUtils.asCollection(ServerParameterUtils.getJBossServerTypeParamterers());
	}
	
	public DeploymentScannerAdditionsTest(String serverType) {
		this.serverType = serverType;
	}
	
	@After
	public void tearDown() throws Exception {
		ASMatrixTests.cleanup();
	}

	@Test
	public void testMetadata() {
		try {
			IServer s = ServerCreationTestUtils.createMockServerWithRuntime(serverType, serverType);
			IServerWorkingCopy wc = s.createWorkingCopy();
			wc.setAttribute(IDeployableServer.DEPLOY_DIRECTORY_TYPE, IDeployableServer.DEPLOY_METADATA);
			s = wc.save(true, null);
			String[] folders = getAdditions().getDeployLocationFolders(s);
			assertEquals(1, folders.length);
			assertTrue(folders[0].contains("metadata"));
		} catch(CoreException ce) {
			fail("Unable to save changes to server");
		}
	}


	@Test
	public void testCustom() {
		try {
			IServer s = ServerCreationTestUtils.createMockServerWithRuntime(serverType, serverType);
			IServerWorkingCopy wc = s.createWorkingCopy();
			wc.setAttribute(IDeployableServer.DEPLOY_DIRECTORY_TYPE, IDeployableServer.DEPLOY_CUSTOM);
			wc.setAttribute(IDeployableServer.DEPLOY_DIRECTORY, "/home/user/test");
			s = wc.save(true, null);
			String[] folders = getAdditions().getDeployLocationFolders(s);
			assertEquals(1, folders.length);
			assertTrue(folders[0].equals("/home/user/test"));
		} catch(CoreException ce) {
			fail("Unable to save changes to server");
		}
	}

	private static int projNum = 0;
	@Test
	public void testMetadataWithCustomModule() {
		try {
			// Create the server
			IServer s = ServerCreationTestUtils.createMockServerWithRuntime(serverType, serverType);
			IServerWorkingCopy wc = s.createWorkingCopy();
			wc.setAttribute(IDeployableServer.DEPLOY_DIRECTORY_TYPE, IDeployableServer.DEPLOY_METADATA);
			s = wc.save(true, null);

			// Create the project
			projNum++;
			String projName = "Project" + projNum;
			IDataModel dm = CreateProjectOperationsUtility.getWebDataModel(projName, 
					null, null, null, null, JavaEEFacetConstants.WEB_24, false);
			try {
				OperationTestCase.runAndVerify(dm);
			} catch(Exception e) {
				fail("Unable to create test web project: " + e.getMessage());
			}
			IProject p = ResourceUtils.findProject(projName);
			assertTrue(p.exists());
			IModule projMod = ServerUtil.getModule(p);
			
			// Set the custom setting. For now, just override output name
			wc = s.createWorkingCopy();
			AbstractPublishingTest.setCustomDeployOverride(wc, projMod, 
					"newName.war", null, null);
			s = wc.save(true,  null);
			
			// verify no change vs standard metadata
			String[] folders = getAdditions().getDeployLocationFolders(s);
			assertEquals(1, folders.length);
			assertTrue(folders[0].contains("metadata"));

			
			// Change it to now make some other output folder
			wc = s.createWorkingCopy();
			AbstractPublishingTest.setCustomDeployOverride(wc, projMod, 
					"newName.war", "/home/user/deploy", null);
			s = wc.save(true,  null);
			folders = getAdditions().getDeployLocationFolders(s);
			assertEquals(2, folders.length);
			assertTrue(folders[0].contains("metadata"));
			assertTrue(folders[1].equals("/home/user/deploy"));

		} catch(CoreException ce) {
			fail("Unable to save changes to server");
		}
	}
	
	@Test
	public void testRemoteGetsNoAdditionsInvalidMode() {
		// This test ensures that 'remote' servers are not handled by the standard listeners
		IServer s = ServerCreationTestUtils.createMockServerWithRuntime(serverType, serverType);
		try {
			IServerWorkingCopy wc = s.createWorkingCopy();
			wc.setAttribute(IDeployableServer.SERVER_MODE, "mock5unknown");
			s = wc.save(true, null);
		} catch(CoreException ce) {
			fail("Could not set server mode to non-local");
		}
		boolean accepts = new LocalJBoss7DeploymentScannerAdditions().accepts(s);
		assertFalse(accepts);
		accepts = new JMXServerDeploymentScannerAdditions().accepts(s);
		assertFalse(accepts);

		Job j1 = new LocalJBoss7DeploymentScannerAdditions().getUpdateDeploymentScannerJob(s);
		assertNull(j1);
		Job j2 = new JMXServerDeploymentScannerAdditions().getUpdateDeploymentScannerJob(s);
		assertNull(j2);
	}

	@Test
	public void testNoAdditionsRemovalsFromSettings() {
		// This test ensures that 'remote' servers are not handled by the standard listeners
		IServer s = ServerCreationTestUtils.createMockServerWithRuntime(serverType, serverType);
		try {
			IServerWorkingCopy wc = s.createWorkingCopy();
			wc.setAttribute(IJBossToolingConstants.PROPERTY_ADD_DEPLOYMENT_SCANNERS, false);
			wc.setAttribute(IJBossToolingConstants.PROPERTY_REMOVE_DEPLOYMENT_SCANNERS, false);
			s = wc.save(true, null);
		} catch(CoreException ce) {
			fail("Could not set server mode to non-local");
		}
		
		// Accepts will also check if the proper server type is here
		ServerExtendedProperties props = (ServerExtendedProperties)s.loadAdapter(ServerExtendedProperties.class, null);
		boolean usesManagement = props != null && 
				props.getMultipleDeployFolderSupport() == ServerExtendedProperties.DEPLOYMENT_SCANNER_AS7_MANAGEMENT_SUPPORT;
		boolean usesJMX = props != null && 
				props.getMultipleDeployFolderSupport() == ServerExtendedProperties.DEPLOYMENT_SCANNER_JMX_SUPPORT;

		
		boolean accepts = new LocalJBoss7DeploymentScannerAdditions().accepts(s);
		assertEquals(accepts, usesManagement);
		accepts = new JMXServerDeploymentScannerAdditions().accepts(s);
		assertEquals(accepts, usesJMX);

		Job j1 = new LocalJBoss7DeploymentScannerAdditions().getUpdateDeploymentScannerJob(s);
		assertNull(j1);
		Job j2 = new JMXServerDeploymentScannerAdditions().getUpdateDeploymentScannerJob(s);
		assertNull(j2);
		Job j3 = new LocalJBoss7DeploymentScannerAdditions().getRemoveDeploymentScannerJob(s);
		assertNull(j3);
		Job j4 = new JMXServerDeploymentScannerAdditions().getRemoveDeploymentScannerJob(s);
		assertNull(j4);
	}

	private AbstractDeploymentScannerAdditions getAdditions() {
		return new AbstractDeploymentScannerAdditions() {
			protected void ensureScannersAdded(IServer server, String[] folders) {
				// do nothing
			}
			public boolean accepts(IServer server) {
				return true;
			}
		};
	}
}
