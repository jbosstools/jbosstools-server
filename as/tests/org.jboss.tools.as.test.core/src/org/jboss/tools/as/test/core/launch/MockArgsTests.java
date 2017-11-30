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

import java.io.File;
import java.util.Collection;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaRuntime;
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
	@Parameters(name = "{0}")
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
	protected IServer checkDefaultArgs() throws CoreException {
		IServer server = ServerCreationTestUtils.createMockServerWithRuntime(serverType, serverType);
		IServer fixed = setMockDetails(server);
		
		ILaunchConfiguration lc = server.getLaunchConfiguration(true, new NullProgressMonitor());
		String progArgs = getProgramArguments(lc);
		String vmArgs = getVMArguments(lc);
		
		try {
			JBossExtendedProperties props = (JBossExtendedProperties)server.loadAdapter(JBossExtendedProperties.class, new NullProgressMonitor());
			String defaultArgs = props.getDefaultLaunchArguments().getStartDefaultProgramArgs().replace("\"", "");
			String defaultVMArgs = props.getDefaultLaunchArguments().getStartDefaultVMArgs().replace("\"", "");
			defaultArgs = defaultArgs.trim().replaceAll("[ ]+", " ");
			defaultVMArgs = defaultVMArgs.replace(server.getRuntime().getRuntimeType().getId(), serverType);
			defaultVMArgs = defaultVMArgs.trim().replaceAll("[ ]+", " ");
			
			String safeQuotesArgs = progArgs.replace("\"", "").replaceAll("[ ]+", " ");
			String safeQuotesVM = vmArgs.replace("\"", "").replaceAll("[ ]+", " ");
			
			assertTrue(safeQuotesArgs + "    should contain    " + defaultArgs,   safeQuotesArgs.contains(defaultArgs));
			assertTrue(safeQuotesVM   + "    should contain    " + defaultVMArgs, safeQuotesVM.contains(defaultVMArgs));
		} catch(Exception e) {
			e.printStackTrace();
		}
		return fixed;
	}

	@Test
	public void testRemoveCriticalVMArgs() {
		try {
			IServer server = checkDefaultArgs();
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
	
	
	
	

	public String getProgramArguments(ILaunchConfiguration configuration)
			throws CoreException {
		String arguments = configuration.getAttribute(
				IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, ""); //$NON-NLS-1$
		return VariablesPlugin.getDefault().getStringVariableManager()
				.performStringSubstitution(arguments);
	}
	public String[] getJavaLibraryPath(ILaunchConfiguration configuration) throws CoreException {
		IJavaProject project = getJavaProject(configuration);
		if (project != null) {
			String[] paths = JavaRuntime.computeJavaLibraryPath(project, true);
			if (paths.length > 0) {
				return paths;
			}
		}
		return null;
	}
	public IJavaProject getJavaProject(ILaunchConfiguration configuration)
			throws CoreException {
		String projectName = getJavaProjectName(configuration);
		if (projectName != null) {
			projectName = projectName.trim();
			if (projectName.length() > 0) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot()
						.getProject(projectName);
				IJavaProject javaProject = JavaCore.create(project);
				if (javaProject != null && javaProject.exists()) {
					return javaProject;
				}
			}
		}
		return null;
	}
	public String getJavaProjectName(ILaunchConfiguration configuration)
			throws CoreException {
		return configuration.getAttribute(
				IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
				(String) null);
	}

	public String getVMArguments(ILaunchConfiguration configuration) throws CoreException {
		String arguments = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, ""); //$NON-NLS-1$
		String args = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(arguments);
		int libraryPath = args.indexOf("-Djava.library.path"); //$NON-NLS-1$
		if (libraryPath < 0) {
			// if a library path is already specified, do not override
			String[] javaLibraryPath = getJavaLibraryPath(configuration);
			if (javaLibraryPath != null && javaLibraryPath.length > 0) {
				StringBuffer path = new StringBuffer(args);
				path.append(" -Djava.library.path="); //$NON-NLS-1$
				path.append("\""); //$NON-NLS-1$
				for (int i = 0; i < javaLibraryPath.length; i++) {
					if (i > 0) {
						path.append(File.pathSeparatorChar);
					}
					path.append(javaLibraryPath[i]);
				}
				path.append("\""); //$NON-NLS-1$
				args = path.toString();
			}
		}
		return args;
	}	
}
