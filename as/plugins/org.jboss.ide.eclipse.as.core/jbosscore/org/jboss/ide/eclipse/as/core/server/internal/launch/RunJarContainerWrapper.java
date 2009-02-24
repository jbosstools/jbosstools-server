package org.jboss.ide.eclipse.as.core.server.internal.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;

public class RunJarContainerWrapper {
	public static final String ID = "org.jboss.ide.eclipse.as.core.server.launch.runJarContainer";
	public static final String RESOLVER_ID = "org.jboss.ide.eclipse.as.core.server.internal.launch.runtimeCPEResolver";

	public static class RunJarContainerInitializer extends ClasspathContainerInitializer { 
		public void initialize(IPath containerPath, IJavaProject project)
				throws CoreException {
			RunJarContainer container = new RunJarContainer(containerPath);
			JavaCore.setClasspathContainer(containerPath, 
					new IJavaProject[] {project}, new IClasspathContainer[] {container}, null);
		}
	}
	
	public static class RunJarResolver implements org.eclipse.jdt.launching.IRuntimeClasspathEntryResolver {

		public IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(
				IRuntimeClasspathEntry entry, ILaunchConfiguration configuration)
				throws CoreException {
			IPath p = entry.getPath();
			IClasspathEntry[] entries = new RunJarContainer(p).getClasspathEntries();
			IRuntimeClasspathEntry[] rtEntries = new IRuntimeClasspathEntry[entries.length];
			for( int i = 0; i < entries.length; i++ ) {
				rtEntries[i] = JavaRuntime.newArchiveRuntimeClasspathEntry(entries[i].getPath());
			}
			return rtEntries;
		}

		public IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(
				IRuntimeClasspathEntry entry, IJavaProject project)
				throws CoreException {
			return null;
		}

		public IVMInstall resolveVMInstall(IClasspathEntry entry)
				throws CoreException {
			return null;
		}
		
	}
	
	public static class RunJarContainer implements IClasspathContainer {
		protected IPath path;
		public RunJarContainer(IPath path) {
			this.path = path;
		}
		
		public IClasspathEntry[] getClasspathEntries() {
			String name = path.segment(1);
			IServer[] servers = ServerCore.getServers();
			IServer s = null;
			for( int i = 0; i < servers.length; i++ ) {
				if( servers[i].getName().equals(name))
					s = servers[i];
			}
			if( s != null ) {
				IRuntime rt = s.getRuntime();
				IPath home = rt.getLocation();
				IPath runJar = home.append(JBossServerStartupLaunchConfiguration.START_JAR_LOC);
				
				return new IClasspathEntry[] {
						JavaRuntime.newArchiveRuntimeClasspathEntry(
								runJar).getClasspathEntry()
				};
			}
			return new IClasspathEntry[]{};
		}

		public String getDescription() {
			return null;
		}

		public int getKind() {
			return K_APPLICATION;
		}

		public IPath getPath() {
			return path;
		}
		
	}
}
