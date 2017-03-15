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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllableServerBehavior;
import org.jboss.tools.as.core.server.controllable.IDeployableServerBehaviorProperties;
import org.jboss.tools.as.test.core.ASMatrixTests;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;
import org.jboss.tools.as.test.core.internal.utils.ServerParameterUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class MockArgsTests extends TestCase  {
	private String serverType;
	private IServer server;
	@Parameters
	public static Collection<Object[]> data() {
		 return ServerParameterUtils.asCollection(ServerParameterUtils.getJBossServerTypeParameters());
	}
	
	public MockArgsTests(String serverType) {
		this.serverType = serverType;
	}
	
	@After
	public void tearDown() throws Exception {
		ASMatrixTests.cleanup();
	}
	
	// This will actually try to launch a process, but against a mock server
	// A process will return (or should) but the process would terminate immediately, 
	// letting us inspect it's arguments
	protected IServer runAndVerifyArgs() {
		IServer server = ServerCreationTestUtils.createMockServerWithRuntime(serverType, serverType);
		IServer fixed = setMockDetails(server);
		
		IProcess p = runAndGetProcess(server);
		assertNotNull("Process must not be null", p);
		String command =  (p == null ? null : p.getAttribute(IProcess.ATTR_CMDLINE));

		assertFalse("No args found from process for server type " + server.getServerType().getId(), 
				command == null || command.trim().length() == 0);
		
		try {
			JBossExtendedProperties props = (JBossExtendedProperties)server.loadAdapter(JBossExtendedProperties.class, new NullProgressMonitor());
			String defaultArgs = props.getDefaultLaunchArguments().getStartDefaultProgramArgs().replace("\"", "");
			String defaultVMArgs = props.getDefaultLaunchArguments().getStartDefaultProgramArgs().replace("\"", "");
			String safeQuotes = command.replace("\"", "").replaceAll("[ ]+", " ");
			defaultArgs = defaultArgs.trim().replaceAll("[ ]+", " ");
			assertTrue(safeQuotes + " should contain " + defaultArgs, safeQuotes.contains(defaultArgs));
			
			// This is done bc the only difference here will be in the "program name" argument, 
			// which gets the runtime name or server name. In this case, it's the runtime name, since
			// the API being used is deprecated and does not have reference to a server
			defaultVMArgs = defaultVMArgs.replace(server.getRuntime().getRuntimeType().getId(), serverType);
			defaultVMArgs = defaultVMArgs.trim().replaceAll("[ ]+", " ");
			assertTrue(safeQuotes.contains(defaultVMArgs));
		} catch(Exception e) {
			e.printStackTrace();
		}
		return fixed;
	}

	@Test
	public void testRemoveCriticalVMArgs() {
		IServer server = runAndVerifyArgs();
		try {
			ILaunchConfiguration config = server.getLaunchConfiguration(true, new NullProgressMonitor());
			ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "hello");
			wc.doSave();
			
			// re-get it and check the changes
			ILaunchConfiguration launchConfig = server.getLaunchConfiguration(false, null);
			String vmArgs = launchConfig.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, (String)null);
			assertFalse(vmArgs == null);
			
			JBossExtendedProperties props = (JBossExtendedProperties)server.loadAdapter(JBossExtendedProperties.class, new NullProgressMonitor());
			String defaultVMArgs = props.getDefaultLaunchArguments().getStartDefaultProgramArgs().replace("\"", "");

			assertFalse(vmArgs.equals(defaultVMArgs));
			
			// Assert that some other args were put back in
			assertTrue(vmArgs.trim().length() > "hello".length());
		} catch(CoreException ce) {
			fail(ce.getMessage());
		}
	}
	
	protected IProcess runAndGetProcess(final IServer server) {
		try {
			server.start("run", new NullProgressMonitor());
		} catch( CoreException ce) {}
		
		int loops = 0;
		ControllableServerBehavior behavior = (ControllableServerBehavior)server.loadAdapter(ControllableServerBehavior.class, null);
		
		while(loops < 500) {
			Object p = behavior.getSharedData(IDeployableServerBehaviorProperties.PROCESS);
			if( p != null ) {
				assertTrue(p instanceof IProcess);
				return (IProcess)p;
			}
			try {
				loops++;
				runEventLoop(100);
			} catch(Exception e){}
		}
		return null;
	}
	
	private void runEventLoop(long ms) {
		long cur = System.currentTimeMillis();
		long dest = cur + ms;
		Display d = Display.getCurrent();
		while(System.currentTimeMillis() < dest && d != null && !d.isDisposed()) {
			if (!Display.getCurrent().readAndDispatch()) {
				try {
					Thread.sleep(10);
				} catch(InterruptedException ie) {}
			}
		}
	}
	
	protected String runAndGetCommand(final IServer server) {
		IProcess p = runAndGetProcess(server);
		return p == null ? null : p.getAttribute(IProcess.ATTR_CMDLINE);
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
}
