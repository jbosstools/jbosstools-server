/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ide.eclipse.as.core.server.runtime;

import java.lang.reflect.Constructor;
import java.util.Hashtable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.internal.RuntimeWorkingCopy;
import org.eclipse.wst.server.core.model.RuntimeDelegate;


/**
 * This class is pretty stale and I dont even remember
 * how important it is anymore. I do know that the version delegate
 * it references is very important, so go look at that class.
 * 
 * @author rstryker
 *
 */
public class JBossServerRuntime extends RuntimeDelegate {

	public static String PROPERTY_VM_ID = "PROPERTY_VM_ID";
	public static String PROPERTY_VM_TYPE_ID = "PROPERTY_VM_TYPE_ID";
	
	public static String PROPERTY_CONFIGURATION_NAME = "PROPERTY_CONFIG_NAME_";
	
	
	private static Hashtable versionDelegates;
	private AbstractServerRuntimeDelegate versionDelegate;

	static {
		versionDelegates = new Hashtable();
		versionDelegates.put (JBoss32RuntimeDelegate.VERSION_ID, JBoss32RuntimeDelegate.class);
		versionDelegates.put (JBoss40RuntimeDelegate.VERSION_ID, JBoss40RuntimeDelegate.class);		
	}
	
	public JBossServerRuntime() {
	}

	protected void initialize() {
	}

	public void dispose() {
		// do nothing
	}
	
	
	public void setConfigName(String config) {
		IRuntimeWorkingCopy copy = getRuntimeWorkingCopy();
		if( copy instanceof RuntimeWorkingCopy ) {
			((RuntimeWorkingCopy)copy).setAttribute(PROPERTY_CONFIGURATION_NAME, config);
			try {
				copy.save(true, new NullProgressMonitor());
			} catch( CoreException ce ) {
			}
		}
	}
	
	public String getConfigName() {
		return getAttribute(PROPERTY_CONFIGURATION_NAME, (String)"");
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
	
	public void setLocation(String loc) {
		IRuntimeWorkingCopy copy = getRuntimeWorkingCopy();
		if( copy instanceof RuntimeWorkingCopy ) {
			((RuntimeWorkingCopy)copy).setLocation(new Path(loc));
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
		
		return null;
	}
	
	
	private void createVersionDelegate(String id) {
		Class delegateClass = (Class) versionDelegates.get(id);
		versionDelegate = newDelegateInstance(delegateClass);
	}
	
	private AbstractServerRuntimeDelegate newDelegateInstance(Class delegateClass) {
		if (delegateClass !=  null) {
			try {
				Constructor constructor = delegateClass.getConstructor(new Class[] { JBossServerRuntime.class });
				if (constructor != null) {
					return (AbstractServerRuntimeDelegate) 
						constructor.newInstance(new Object[] { this });					
				}
			}
			catch (Exception e)	{
				e.printStackTrace();
			}
		}
		return null;
	}

	
	public AbstractServerRuntimeDelegate getVersionDelegate() {
		if( versionDelegate == null ) {
			createVersionDelegate(getRuntime().getRuntimeType().getId());
		}
		return versionDelegate;
	}

}
