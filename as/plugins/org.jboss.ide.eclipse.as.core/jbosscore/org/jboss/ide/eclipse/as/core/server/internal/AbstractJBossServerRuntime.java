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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.internal.Messages;
import org.eclipse.wst.server.core.internal.RuntimeWorkingCopy;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.core.model.RuntimeDelegate;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.runtime.IJBossServerRuntime;

public abstract class AbstractJBossServerRuntime extends RuntimeDelegate implements IJBossServerRuntime {

	public void setDefaults(IProgressMonitor monitor) {
		getRuntimeWorkingCopy().setLocation(new Path(""));
	}

	public IStatus validate() {
		IStatus s = super.validate();
		if( !s.isOK()) return s;
		
		if( getJBossConfiguration().equals(""))
			return new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 0, "Runtime Configuration Not Set", null);
		
		return Status.OK_STATUS;
	}
	
	public void setVMInstall(IVMInstall selectedVM) {
		IRuntimeWorkingCopy copy = getRuntimeWorkingCopy();
		if( copy instanceof RuntimeWorkingCopy ) {
			((RuntimeWorkingCopy)copy).setAttribute(PROPERTY_VM_ID, selectedVM.getId());
			((RuntimeWorkingCopy)copy).setAttribute(PROPERTY_VM_TYPE_ID, selectedVM.getVMInstallType().getId());
			try {
				copy.save(true, new NullProgressMonitor());
			} catch( CoreException ce ) {
				
			}
		}
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
	
	public String getJBossConfiguration() {
		return getAttribute(PROPERTY_CONFIGURATION_NAME, (String)"");
	}
	
	public abstract String getId();
}
