package org.jboss.ide.eclipse.as.core.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServerAttributes;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.internal.RuntimeWorkingCopy;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.wtp.core.ASWTPToolsPlugin;
import org.jboss.ide.eclipse.as.wtp.core.Messages;

public class RuntimeUtils {
	
	public static IJBossServerRuntime getJBossServerRuntime(IServerAttributes server) {
		IRuntime rt = null;
		if (server != null) {
			rt = server.getRuntime();
		}
		IJBossServerRuntime jbrt = null;
		if (rt != null)
			jbrt = (IJBossServerRuntime) rt.loadAdapter(IJBossServerRuntime.class, new NullProgressMonitor());
		return jbrt;
	}

	public static IJBossServerRuntime checkedGetJBossServerRuntime(IServerAttributes server) throws CoreException {
		IJBossServerRuntime jbrt = getJBossServerRuntime(server);
		if (jbrt == null)
			throw new CoreException(new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID,
					NLS.bind(Messages.ServerRuntimeNotFound, server.getName())));
		return jbrt;
	}
	
	public static IJBossServerRuntime getJBossServerRuntime(IRuntime runtime) {
		if (runtime == null) {
			return null;
		}
		return (IJBossServerRuntime) runtime.loadAdapter(IJBossServerRuntime.class, new NullProgressMonitor());
	}
	
	public static boolean isEAP(IRuntime runtime) {
		IJBossServerRuntime jbossRuntime = getJBossServerRuntime(runtime);
		if (jbossRuntime == null) {
			return false;
		}
		return jbossRuntime.isEAP();
		
	}
	
	public static IJBossServerRuntime checkedGetJBossServerRuntime(IRuntime runtime) throws CoreException {
		IJBossServerRuntime jbossRuntime = getJBossServerRuntime(runtime);
		if (jbossRuntime == null)
			throw new CoreException(new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID,
					NLS.bind(Messages.ServerRuntimeNotFound, ((runtime==null)?"null":runtime.getName())))); //$NON-NLS-1$
		return jbossRuntime;
	}
	
	public static IRuntime createRuntime(String runtimeId, String homeDir,
			String config, IVMInstall install) throws CoreException {
		IRuntimeType[] runtimeTypes = ServerUtil.getRuntimeTypes(null, null,runtimeId);
		IRuntimeType runtimeType = runtimeTypes[0];
		IRuntimeWorkingCopy runtimeWC = runtimeType.createRuntime(null,
				new NullProgressMonitor());
		runtimeWC.setName(runtimeId);
		runtimeWC.setLocation(new Path(homeDir));
		((RuntimeWorkingCopy) runtimeWC).setAttribute(
				IJBossServerRuntime.PROPERTY_VM_ID, install.getId());
		((RuntimeWorkingCopy) runtimeWC).setAttribute(
				IJBossServerRuntime.PROPERTY_VM_TYPE_ID, install
						.getVMInstallType().getId());
		((RuntimeWorkingCopy) runtimeWC).setAttribute(
				IJBossServerRuntime.PROPERTY_CONFIGURATION_NAME, config);

		IRuntime savedRuntime = runtimeWC.save(true, new NullProgressMonitor());
		return savedRuntime;
	}
	
	public static IRuntime createRuntime(String runtimeId, String homeDir,
			String config) throws CoreException {
		return createRuntime(runtimeId, homeDir, config, JavaRuntime.getDefaultVMInstall());
	}
	
	
}
