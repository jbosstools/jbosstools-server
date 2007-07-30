package org.jboss.ide.eclipse.as.core.runtime.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jst.server.core.RuntimeClasspathProviderDelegate;
import org.eclipse.wst.server.core.IRuntime;
import org.jboss.ide.eclipse.as.core.runtime.server.AbstractJBossServerRuntime;

public class ClientAllRuntimeClasspathProvider extends RuntimeClasspathProviderDelegate {

	public IClasspathEntry[] resolveClasspathContainer(IProject project, IRuntime runtime) {
		AbstractJBossServerRuntime rt = ((AbstractJBossServerRuntime)  (runtime.loadAdapter(AbstractJBossServerRuntime.class, new NullProgressMonitor())));
		String id = rt == null ? "" : rt.getId();
		IPath loc = runtime.getLocation();
		if( id.indexOf("4.2") > -1 ) {
			return new IClasspathEntry[] { JavaCore.newLibraryEntry(loc.append("client").append("jbossall-client.jar"), null, null) };
		} else if( id.indexOf("4.0") > -1 ) {
			return new IClasspathEntry[] { JavaCore.newLibraryEntry(loc.append("client").append("jbossall-client.jar"), null, null) };
		} else if( id.indexOf("3.2") > -1 ) { 
			return new IClasspathEntry[] { JavaCore.newLibraryEntry(loc.append("client").append("jbossall-client.jar"), null, null) };
		}
		return null;
	}
}
