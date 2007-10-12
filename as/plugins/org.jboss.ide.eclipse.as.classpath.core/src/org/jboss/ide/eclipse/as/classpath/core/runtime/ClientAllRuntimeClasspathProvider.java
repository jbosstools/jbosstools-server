package org.jboss.ide.eclipse.as.classpath.core.runtime;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jst.server.core.RuntimeClasspathProviderDelegate;
import org.eclipse.wst.server.core.IRuntime;
import org.jboss.ide.eclipse.as.classpath.core.ClasspathCorePlugin;
import org.jboss.ide.eclipse.as.core.server.internal.AbstractJBossServerRuntime;

/**
 * This class uses the "throw everything you can find" strategy
 * in providing additions to the classpath.  Given a server runtime, 
 * it will try to add whatever could possibly ever be used.
 * 
 * @author Rob Stryker
 *
 */
public class ClientAllRuntimeClasspathProvider extends
		RuntimeClasspathProviderDelegate {

	public ClientAllRuntimeClasspathProvider() {
		// TODO Auto-generated constructor stub
	}

	public IClasspathEntry[] resolveClasspathContainer(IProject project, IRuntime runtime) {
		if( runtime == null ) 
			return null;

		AbstractJBossServerRuntime ajbsrt = (AbstractJBossServerRuntime)runtime.loadAdapter(AbstractJBossServerRuntime.class, new NullProgressMonitor());
		if( ajbsrt == null ) {
			// log error
			IStatus status = new Status(IStatus.WARNING, ClasspathCorePlugin.PLUGIN_ID, "Runtime " + runtime.getName() + "is not of the proper type");
			ClasspathCorePlugin.getDefault().getLog().log(status);
			return null;
		}
		
		IPath loc = runtime.getLocation();
		String config = ajbsrt.getJBossConfiguration();
		if( runtime.getRuntimeType().getId().endsWith("32")) {
			return get32(loc, config);
		} else if( runtime.getRuntimeType().getId().endsWith("40")) {
			return get40(loc, config);
		} else if( runtime.getRuntimeType().getId().endsWith("42")) {
			return get42(loc, config);
		}
		return null;
	}
	
	protected IClasspathEntry[] get32(IPath location, String config) {
		ArrayList list = new ArrayList();
		IPath configPath = location.append("server").append(config);
		addEntries(location.append("client"), list);
		addEntries(location.append("lib"), list);
		addEntries(configPath.append("lib"), list);
		return (IClasspathEntry[]) list.toArray(new IClasspathEntry[list.size()]);
	}
	
	protected IClasspathEntry[] get40(IPath location, String config) {
		ArrayList list = new ArrayList();
		IPath configPath = location.append("server").append(config);
		IPath deployPath = configPath.append("deploy");
		addEntries(location.append("client"), list);
		addEntries(location.append("lib"), list);
		addEntries(configPath.append("lib"), list);
		addEntries(deployPath.append("jboss-web.deployer").append("jsf-libs"), list);
		addEntries(deployPath.append("jboss-aop-jdk50.deployer"), list);
		addEntries(deployPath.append("ejb3.deployer"), list);
		return (IClasspathEntry[]) list.toArray(new IClasspathEntry[list.size()]);
	}

	protected IClasspathEntry[] get42(IPath location, String config) {
		ArrayList list = new ArrayList();
		IPath configPath = location.append("server").append(config);
		IPath deployPath = configPath.append("deploy");
		addEntries(location.append("client"), list);
		addEntries(location.append("lib"), list);
		addEntries(configPath.append("lib"), list);
		addEntries(deployPath.append("jboss-web.deployer").append("jsf-libs"), list);
		addEntries(deployPath.append("jboss-aop-jdk50.deployer"), list);
		addEntries(deployPath.append("ejb3.deployer"), list);
		return (IClasspathEntry[]) list.toArray(new IClasspathEntry[list.size()]);
	}

	
	protected IClasspathEntry getEntry(IPath path) {
		return JavaRuntime.newArchiveRuntimeClasspathEntry(path).getClasspathEntry();
	}
	protected void addEntries(IPath folder, ArrayList list) {
		if( folder.toFile().exists()) {
			String[] files = folder.toFile().list();
			for( int i = 0; i < files.length; i++ ) {
				if( files[i].endsWith(".jar")) {
					list.add(getEntry(folder.append(files[i])));
				}
			}
		}
	}

}
