/*******************************************************************************
 * Copyright (c) 2016 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.test.core.utiltests;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.util.RemotePath;
import org.jboss.ide.eclipse.as.core.util.ServerHomeValidationUtility;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IFilesystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IServerDetailsController;
import org.jboss.tools.as.core.server.controllable.systems.IDeploymentOptionsController;
import org.junit.Test;

import junit.framework.TestCase;

/**
 * This test just makes sure the server hoem validation
 * will only request filesystem and serverdetails subsystems, 
 * and will not request deploymentoptions as some profiles don't have it. 
 *
 */
public class ServerHomeValidationUtilityTest extends TestCase {

	
	@Test
	public void testMockServerHomeValidator() throws CoreException {
		IStatus ret = internalValidation("/home/user/test", "/", true);
		assertTrue(ret.isOK());
	}

	@Test
	public void testMockServerNullHome() throws CoreException {
		IStatus ret = internalValidation(null, "/", true);
		assertTrue(ret.getSeverity() == IStatus.ERROR);
	}

	@Test
	public void testMockServerNullSep() throws CoreException {
		IStatus ret = internalValidation("/home/user/test", null, true);
		assertTrue(ret.getSeverity() == IStatus.ERROR);
	}

	@Test
	public void testMockServerDNE() throws CoreException {
		IStatus ret = internalValidation("/home/user/test", "/", false);
		assertTrue(ret.getSeverity() == IStatus.ERROR);
	}

	@Test
	public void testMockServerDeploymentOptions() throws CoreException {
		try {
			IStatus ret = internalValidation("/home/user/test", "/", true, true);
			fail(); // should not reach
		} catch(CoreException ce) {
			// ignore
		}
	}

	private IStatus internalValidation(String home, String sep, boolean exists) throws CoreException {
		return internalValidation(home, sep, exists, false);
	}
	
	private IStatus internalValidation(String home, String sep, boolean exists, boolean loadDeploymentOptions) throws CoreException {
		
		IServer s = mock(IServer.class);
		ControllableServerBehavior delegate = mock(ControllableServerBehavior.class);
		when(s.loadAdapter(ServerBehaviourDelegate.class, null)).thenReturn(delegate);
		when(s.getAdapter(ServerBehaviourDelegate.class)).thenReturn(delegate);
		
		IFilesystemController fs = mock(IFilesystemController.class);
		IServerDetailsController details = mock(IServerDetailsController.class);
		
		when(delegate.getController(IFilesystemController.SYSTEM_ID)).thenReturn(fs);
		when(delegate.getController(IServerDetailsController.SYSTEM_ID)).thenReturn(details);
		when(delegate.getController(IDeploymentOptionsController.SYSTEM_ID)).thenThrow(
				new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Fail test")));
		
		
		when(details.getProperty(IServerDetailsController.PROP_SERVER_HOME)).thenReturn(home);
		when(details.getProperty(IServerDetailsController.SEPARATOR_CHAR)).thenReturn(sep);
		
		
		IPath remoteHome = null;
		if( sep != null && home != null ) 
			remoteHome = new RemotePath(home, sep); 
		
		when(fs.exists(any(RemotePath.class), 
				any(IProgressMonitor.class))).thenReturn(exists);
		
		if( loadDeploymentOptions ) {
			delegate.getController(IDeploymentOptionsController.SYSTEM_ID);
		}
		
		
		IStatus ret = new ServerHomeValidationUtility().validateServerHome(s, false);
		return ret;
	}
}
