/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.launching.environments.EnvironmentsManager;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.model.RuntimeDelegate;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.IConstants;
import org.jboss.ide.eclipse.as.wtp.core.util.VMInstallUtil;

public abstract class AbstractLocalJBossServerRuntime extends RuntimeDelegate implements IJBossServerRuntime {
	
	public void setDefaults(IProgressMonitor monitor) {
		getRuntimeWorkingCopy().setName(getNextRuntimeName());
		setExecutionEnvironment(getDefaultExecutionEnvironment(getRuntime().getRuntimeType()));
		setVM(null);
	}
	
	protected String getNextRuntimeName() {
		return getNextRuntimeName(getRuntimeNameBase());
	}
	
	protected String getRuntimeNameBase() {
		String version = getRuntime().getRuntimeType().getVersion(); 
		String base = Messages.jboss + " " + version + " " + Messages.runtime;  //$NON-NLS-1$//$NON-NLS-2$
		return base;
	}
	
	public abstract String getDefaultRunArgs();
	public abstract String getDefaultRunVMArgs();
	
	public static String getNextRuntimeName(String base) {
		IRuntime rt = ServerCore.findRuntime(base);
		if (rt == null)
			return base;

		int i = 0;
		while (rt != null) {
			rt = ServerCore.findRuntime(base + " " + ++i); //$NON-NLS-1$
		}
		return base + " " + i; //$NON-NLS-1$
	}
	
	/* Return a vm that is hard-coded in the runtime's attributes*/
	public IVMInstall getHardVM() {
		if (getVMInstallTypeId() != null) {
			String id = getAttribute(PROPERTY_VM_ID, (String)null);
			String type = getAttribute(PROPERTY_VM_TYPE_ID, (String)null);
			return VMInstallUtil.findVMInstall(type, id);
		}
		return null;
	}
	
	public IVMInstall getVM() {
		IVMInstall hard = getHardVM();
		if( hard != null )
			return hard;
		
		if( getExecutionEnvironment() != null ) {
			return VMInstallUtil.findVMInstall(getExecutionEnvironment());
		}
		// not found, return default vm
		return getDefaultVMInstall();
	}
	
	public void setVM(IVMInstall selectedVM) {
		if (selectedVM == null) {
			setAttribute(IJBossServerRuntime.PROPERTY_VM_ID, (String) null);
			setAttribute(IJBossServerRuntime.PROPERTY_VM_TYPE_ID, (String) null);
		} else {
			setAttribute(IJBossServerRuntime.PROPERTY_VM_ID, selectedVM.getId());
			setAttribute(IJBossServerRuntime.PROPERTY_VM_TYPE_ID, selectedVM
					.getVMInstallType().getId());
		}
	}

	public boolean isUsingDefaultJRE() {
		return getVMInstallTypeId() == null;
	}
	
	protected String getVMInstallTypeId() {
		return getAttribute(PROPERTY_VM_TYPE_ID, (String)null);
	}

	protected IVMInstall getDefaultVMInstall() {
		IVMInstall i = getExecutionEnvironment().getDefaultVM();
		return i == null ? JavaRuntime.getDefaultVMInstall() : i;
	}
	
	public IVMInstall[] getValidJREs(IRuntimeType type) {
		return getDefaultExecutionEnvironment(type) == null ? new IVMInstall[0] 
				: getDefaultExecutionEnvironment(type).getCompatibleVMs();
	}
	
	public IExecutionEnvironment getExecutionEnvironment() {
		String id = getAttribute(PROPERTY_EXECUTION_ENVIRONMENT, (String)null);
		return id == null ? getDefaultExecutionEnvironment(getRuntime().getRuntimeType()) : 
			EnvironmentsManager.getDefault().getEnvironment(id);
	}
	
	/**
	 * This is being used to indicate the MINIMUM execution environment, 
	 * not just the default!
	 * 
	 * @param rtType
	 * @return
	 */
	public IExecutionEnvironment getDefaultExecutionEnvironment(IRuntimeType rtType) {
		ServerExtendedProperties sep = new ExtendedServerPropertiesAdapterFactory().getExtendedProperties(rtType);
		if( sep instanceof JBossExtendedProperties) {
			return ((JBossExtendedProperties)sep).getDefaultExecutionEnvironment();
		}
		return EnvironmentsManager.getDefault().getEnvironment("J2SE-1.4"); //$NON-NLS-1$
	}

	public void setExecutionEnvironment(IExecutionEnvironment environment) {
		setAttribute(PROPERTY_EXECUTION_ENVIRONMENT, environment == null ? null : environment.getId());
	}
}
