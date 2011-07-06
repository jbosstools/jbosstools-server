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
package org.jboss.ide.eclipse.as.test.server;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.jboss.ide.eclipse.as.core.extensions.polling.ProcessTerminatedPoller.IProcessProvider;
import org.jboss.ide.eclipse.as.core.server.internal.DelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.LocalJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;

public class MockTests extends TestCase {
	public void setUp() {
	}
	public void tearDown() {
		try {
			ServerRuntimeUtils.deleteAllServers();
			ServerRuntimeUtils.deleteAllRuntimes();
		} catch(CoreException ce) {}
	}
	
	
	public void testRemoveCriticalVMArgs() {
		IServer server = serverTestImpl(IJBossToolingConstants.SERVER_AS_50);
		try {
			ILaunchConfiguration config = server.getLaunchConfiguration(true, new NullProgressMonitor());
			ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "hello");
			wc.doSave();
			
			// re-get it and check the changes
			ILaunchConfiguration launchConfig = server.getLaunchConfiguration(false, null);
			String vmArgs = launchConfig.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, (String)null);
			LocalJBossServerRuntime rt = (LocalJBossServerRuntime)server.getRuntime().loadAdapter(LocalJBossServerRuntime.class, new NullProgressMonitor());
			String defaultVMArgs = rt.getDefaultRunVMArgs();
			assertFalse(vmArgs == null);
			assertFalse(vmArgs.equals(rt.getDefaultRunVMArgs()));
			assertFalse(vmArgs.equals(defaultVMArgs));
			assertTrue(vmArgs.startsWith("hello -Djava.endorsed.dirs=\""));
			assertTrue(vmArgs.endsWith(".metadata/.plugins/org.jboss.ide.eclipse.as.test/mockedServers/server1/lib/endorsed\""));
		} catch(CoreException ce) {
			fail(ce.getMessage());
		}
	}
	
	public void testChangeArgs() {
		// should still match the defaults since the defaults are extremely all required
		IServer server = serverTestImpl(IJBossToolingConstants.SERVER_AS_50);
		try {
			ILaunchConfiguration config = server.getLaunchConfiguration(true, new NullProgressMonitor());
			ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "");
			wc.doSave();
			String command = runAndGetCommand(server);
			assertFalse("No args found from process", command == null);
			LocalJBossServerRuntime rt = (LocalJBossServerRuntime)server.getRuntime().loadAdapter(LocalJBossServerRuntime.class, new NullProgressMonitor());
			String defaultArgs = rt.getDefaultRunArgs().replace("\"", "");
			assertTrue(command.replace("\"", "").contains(defaultArgs));
		} catch(CoreException ce) {
			fail(ce.getMessage());
		}
	}
	
	protected IServer serverTestImpl(String type) {
		IServer server = ServerRuntimeUtils.createMockServerWithRuntime(type, "server1", "default");
		IServer fixed = setMockDetails(server);
		String command = runAndGetCommand(fixed);
		assertFalse("No args found from process", command == null);
		
		LocalJBossServerRuntime rt = (LocalJBossServerRuntime)server.getRuntime().loadAdapter(LocalJBossServerRuntime.class, new NullProgressMonitor());
		String defaultArgs = rt.getDefaultRunArgs().replace("\"", "");
		String defaultVMArgs = rt.getDefaultRunVMArgs().replace("\"", "");
		assertTrue(command.replace("\"", "").contains(defaultArgs.trim()));
		assertTrue(command.replace("\"", "").contains(defaultVMArgs.trim()));
		return fixed;
	}

	protected IProcess runAndGetProcess(final IServer server) {
		try {
			server.start("run", new NullProgressMonitor());
		} catch( CoreException ce) {}
		
		int loops = 0;
		DelegatingServerBehavior behavior = (DelegatingServerBehavior)server.loadAdapter(DelegatingServerBehavior.class, null);
		
		while(loops < 50) {
			if( ((IProcessProvider)behavior.getDelegate()).getProcess() != null ) {
				return ((IProcessProvider)behavior.getDelegate()).getProcess();
			}
			try {
				loops++;
				Thread.sleep(1000);
			} catch(Exception e){}
		}
		return null;
	}
	
	protected String runAndGetCommand(final IServer server) {
		return runAndGetProcess(server).getAttribute(IProcess.ATTR_CMDLINE);
	}
	
	private IServer setMockDetails(IServer server) {
		IServerWorkingCopy copy = server.createWorkingCopy();
		ServerAttributeHelper helper = new ServerAttributeHelper(server, copy);
		helper.setAttribute("start-timeout", "2");
		helper.setAttribute("org.jboss.ide.eclipse.as.core.server.attributes.startupPollerKey", 
				"org.jboss.ide.eclipse.as.core.runtime.server.timeoutpoller");
		try {
			return copy.save(true, new NullProgressMonitor());
		} catch( CoreException ce ) {
		}
		return null;
	}
	
	public void test32Mock() {
		serverTestImpl(IJBossToolingConstants.SERVER_AS_32);
	}

	public void test40Mock() {
		serverTestImpl(IJBossToolingConstants.SERVER_AS_40);
	}

	public void test42Mock() {
		serverTestImpl(IJBossToolingConstants.SERVER_AS_42);
	}
	
	public void test50Mock() {
		serverTestImpl(IJBossToolingConstants.SERVER_AS_50);
	}
	public void test51Mock() {
		serverTestImpl(IJBossToolingConstants.SERVER_AS_51);
	}
	public void test60Mock() {
		serverTestImpl(IJBossToolingConstants.SERVER_AS_60);
	}
	public void testEap43Mock() {
		serverTestImpl(IJBossToolingConstants.SERVER_EAP_43);
	}	
	public void testEap50Mock() {
		serverTestImpl(IJBossToolingConstants.SERVER_EAP_50);
	}
}
