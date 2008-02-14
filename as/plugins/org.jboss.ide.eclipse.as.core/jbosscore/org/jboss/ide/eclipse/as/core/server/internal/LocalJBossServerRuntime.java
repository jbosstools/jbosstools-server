/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.core.server.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.model.RuntimeDelegate;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.server.IJBossServerConstants;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;

import sun.security.action.GetLongAction;

public class LocalJBossServerRuntime extends RuntimeDelegate implements IJBossServerRuntime {

	public void setDefaults(IProgressMonitor monitor) {
		String location = Platform.getOS().equals(Platform.WS_WIN32) 
		? "c:/program files/jboss-" : "/usr/bin/jboss-";
		String version = getRuntime().getRuntimeType().getVersion();
		location += version + ".x";
		getRuntimeWorkingCopy().setLocation(new Path(location));
		getRuntimeWorkingCopy().setName(getNextRuntimeName());
		setAttribute(IJBossServerRuntime.PROPERTY_CONFIGURATION_NAME, IJBossServerConstants.DEFAULT_SERVER_NAME);
		setVM(null);
	}

	private String getNextRuntimeName() {
		String version = getRuntime().getRuntimeType().getVersion(); 
		String base = "JBoss " + version + " Runtime";
		IRuntime rt = ServerCore.findRuntime(base);
		if (rt == null)
			return base;

		int i = 0;
		while (rt != null) {
			rt = ServerCore.findRuntime(base + " " + ++i);
		}
		return base + " " + i;
	}

	public IStatus validate() {
		IStatus s = super.validate();
		if( !s.isOK()) return s;
		
		if( getJBossConfiguration().equals(""))
			return new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 0, "Runtime Configuration Not Set", null);
		
		return Status.OK_STATUS;
	}
	
	public IVMInstall getVM() {
		String id = getAttribute(PROPERTY_VM_ID, (String)null);
		String type = getAttribute(PROPERTY_VM_TYPE_ID, (String)null);

		IVMInstallType vmInstallType = JavaRuntime.getVMInstallType(type);
		IVMInstall[] vmInstalls = vmInstallType.getVMInstalls();

		for (int i = 0; i < vmInstalls.length; i++) {
			if (id.equals(vmInstalls[i].getId()))
				return vmInstalls[i];
		}
		
		// not found, return default vm
		return JavaRuntime.getDefaultVMInstall();
	}
	
	public void setVM(IVMInstall selectedVM) {
		if( selectedVM == null )
			selectedVM = JavaRuntime.getDefaultVMInstall();
		
		setAttribute(IJBossServerRuntime.PROPERTY_VM_ID, selectedVM.getId());
		setAttribute(IJBossServerRuntime.PROPERTY_VM_TYPE_ID, selectedVM
						.getVMInstallType().getId());
	}
	
	public String getJBossConfiguration() {
		return getAttribute(PROPERTY_CONFIGURATION_NAME, (String)"");
	}
	
	public void setJBossConfiguration(String config) {
		setAttribute(IJBossServerRuntime.PROPERTY_CONFIGURATION_NAME, config);
	}

	public String getDefaultRunArgs() {
		return "--configuration=" + getJBossConfiguration() + " ";
	}

	public String getDefaultRunVMArgs() {
		String name = getRuntime().getName();
		String ret = "-Dprogram.name=\"JBossTools " + name + "\" ";
		if( Platform.getOS().equals(Platform.OS_MACOSX))
			ret += "-server ";
		ret += "-Xms256m -Xmx512m -XX:MaxPermSize=256m ";
		if( Platform.getOS().equals(Platform.OS_LINUX))
			ret += "-Djava.net.preferIPv4Stack=true ";
		ret += "-Dsun.rmi.dgc.client.gcInterval=3600000 ";
		ret += "-Dsun.rmi.dgc.server.gcInterval=3600000 ";
		ret += "-Djava.endorsed.dirs=" + (getRuntime().getLocation().append("lib").append("endorsed")) + " ";
		
		return ret;
	}
}
