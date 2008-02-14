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
package org.jboss.ide.eclipse.as.core.server;

import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.wst.server.core.IRuntime;

/**
 * 
 * @author Rob Stryker
 */
public interface IJBossServerRuntime {
	public static String PROPERTY_VM_ID = "PROPERTY_VM_ID";
	public static String PROPERTY_VM_TYPE_ID = "PROPERTY_VM_TYPE_ID";
	
	public static String PROPERTY_CONFIGURATION_NAME = "org.jboss.ide.eclipse.as.core.runtime.configurationName";

	public IRuntime getRuntime();
	public IVMInstall getVM();
	public void setVM(IVMInstall install);
	public String getJBossConfiguration();
	public void setJBossConfiguration(String config);
}
