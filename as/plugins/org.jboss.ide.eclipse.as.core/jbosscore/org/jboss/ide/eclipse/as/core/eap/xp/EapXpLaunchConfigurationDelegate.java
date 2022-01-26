/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.core.eap.xp;

import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ID_REMOTE_JAVA_APPLICATION;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.model.Plugin;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.m2e.actions.MavenLaunchConstants;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IProjectConfigurationManager;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;

public class EapXpLaunchConfigurationDelegate implements ILaunchConfigurationDelegate {
	public static final String TYPE = "org.jboss.ide.eclipse.as.core.eapxp.EapXpLaunchConfigurationDelegate";
	
	private static String DEFAULT_PROFILE = "bootable-jar"; 
	private static final String JWDP_HANDSHAKE = "JDWP-Handshake";
	

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		final IProject project = getProject(configuration);
		IMavenProjectFacade facade = MavenPlugin.getMavenProjectRegistry().create( project, new NullProgressMonitor() );
		if( facade == null ) {
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Cannot launch non-maven project"));
		}
		
		// TODO can maybe remove this and instead handle the situation with -D flags on the maven launch
		int port = -1;
		if( "debug".equals(mode)) {
			try {
				port = findPortFromProject(project);
			} catch(CoreException ce) {
				throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Cannot debug project: pom.xml does not set jvmArguments for wildfly-jar configuration"));
			}
			if( port == -1 ) {
				throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Cannot debug project: pom.xml does not set a port in jvmArguments for wildfly-jar configuration"));
			}
		}
		
	    String activeProfiles = DEFAULT_PROFILE;
		IProjectConfigurationManager projectManager = MavenPlugin.getProjectConfigurationManager();
	    try {
	    	activeProfiles = projectManager.getResolverConfiguration(project).getSelectedProfiles();
	    } catch(Exception e) {
	    }
	    if( activeProfiles == null || activeProfiles.length() == 0 )
	    	activeProfiles = DEFAULT_PROFILE;
	    if( !activeProfiles.contains(DEFAULT_PROFILE))
	    	activeProfiles = activeProfiles + "," + DEFAULT_PROFILE;
	    
		ILaunchConfigurationWorkingCopy packageConfig = getConfiguration(project);
		packageConfig.setAttribute(MavenLaunchConstants.ATTR_GOALS, "wildfly-jar:dev-watch");
		packageConfig.setAttribute(MavenLaunchConstants.ATTR_PROFILES, activeProfiles);
		ILaunch buildLaunch = packageConfig.launch("run", new NullProgressMonitor());

		if( "debug".equals(mode)) {
			try {
				port = findPortFromProject(project);
				final int port2 = port;
				new Job("Connecting remote debugger to " + project.getName()) {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						try {
							createRemoteJavaDebugConfiguration(project, port2, new NullProgressMonitor());
						} catch(CoreException ce) {
							
						}
						return Status.OK_STATUS;
					}
					
				}.schedule();
			} catch(CoreException ce) {
				
			}
		}
	}

	private int findPortFromProject(IProject p) throws CoreException {
		IMavenProjectFacade facade = MavenPlugin.getMavenProjectRegistry().create( p, new NullProgressMonitor() );
		if( facade == null ) {
			return -1;
		}
		Plugin plugin = facade.getMavenProject(new NullProgressMonitor()).getPlugin("org.wildfly.plugins:wildfly-jar-maven-plugin");
		int port = -1;
		if (plugin != null) {
			Object config = plugin.getConfiguration();
			if( config instanceof Xpp3Dom) {
				Xpp3Dom o = (Xpp3Dom)plugin.getConfiguration();
				Xpp3Dom[] jvmArgs = o.getChildren("jvmArguments");
				for( int i = 0; jvmArgs != null && i < jvmArgs.length; i++ ) {
					Xpp3Dom[] args = jvmArgs[i].getChildren("arg");
					for( int j = 0; args != null && j < args.length; j++ ) {
						String val = args[j].getValue();
						if( val != null && val.startsWith("-agentlib:")) {
							String v2 = val.substring("-agentlib:".length());
							String[] asArr = v2.split(",");
							for( int k = 0; k < asArr.length; k++ ) {
								if( asArr[k].startsWith("address=")) {
									port = Integer.parseInt(asArr[k].substring("address=".length()));
								}
							}
						}
						System.out.println(val);
					}
				}		
			}
		}
		return port;
	}

	public static ILaunchConfigurationWorkingCopy getConfiguration(IProject project) throws CoreException {
		ILaunchConfigurationType launchConfigurationType = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(MavenLaunchConstants.LAUNCH_CONFIGURATION_TYPE_ID);
		ILaunchConfigurationWorkingCopy launchConfiguration = launchConfigurationType.newInstance(null, project.getName() + "__Maven__");
		launchConfiguration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, "${workspace_loc:/" + project.getName() + "}");
		launchConfiguration.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, new HashMap<>());
		launchConfiguration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH, ProjectUtils.getJREEntry(project));
		launchConfiguration.setAttribute(MavenLaunchConstants.ATTR_WORKSPACE_RESOLUTION, true);
		return launchConfiguration;
	}
	
	public static boolean isMavenProject(IProject project) {
		try {
			return project.hasNature("org.eclipse.m2e.core.maven2Nature");
		} catch (CoreException e) {
			return false;
		}
	}

	private IProject getProject(ILaunchConfiguration configuration) throws CoreException {
		String projectName = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
				(String) null);
		return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
	}

	private ILaunch createRemoteJavaDebugConfiguration(IProject p, int port,
			IProgressMonitor monitor) throws CoreException {
		waitForPortAvailable(port, monitor);
		IProject project = p;
		String name = "Standalone web application remote " + project.getName();
		ILaunchConfigurationType launchConfigurationType = DebugPlugin.getDefault().getLaunchManager()
				.getLaunchConfigurationType(ID_REMOTE_JAVA_APPLICATION);
		ILaunchConfigurationWorkingCopy launchConfiguration = launchConfigurationType.newInstance(null, name);
		launchConfiguration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_ALLOW_TERMINATE, false);
		launchConfiguration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_CONNECTOR,
				IJavaLaunchConfigurationConstants.ID_SOCKET_ATTACH_VM_CONNECTOR);
		Map<String, String> connectMap = new HashMap<>(2);
		connectMap.put("port", String.valueOf(port)); //$NON-NLS-1$
		connectMap.put("hostname", "localhost"); //$NON-NLS-1$ //$NON-NLS-2$
		launchConfiguration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CONNECT_MAP, connectMap);
		launchConfiguration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, project.getName());
		return launchConfiguration.launch("debug", monitor);
	}


	private void waitForPortAvailable(int port, IProgressMonitor monitor) throws CoreException {
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < 60_000 && !monitor.isCanceled()) {
			try (Socket socket = new Socket("localhost", port)) {
				socket.getOutputStream().write(JWDP_HANDSHAKE.getBytes(StandardCharsets.US_ASCII));
				return;
			} catch (ConnectException e) {
				try {
					Thread.sleep(1000L);
				} catch (InterruptedException e1) {
					throw new CoreException(
							new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, e.getLocalizedMessage()));
				}
			} catch (IOException e) {
				throw new CoreException(
						new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, e.getLocalizedMessage()));
			}
		}
		throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Can't connect to JVM"));
	}
}
