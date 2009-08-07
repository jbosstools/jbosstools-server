/**
 * JBoss by Red Hat
 * Copyright 2006-2009, Red Hat Middleware, LLC, and individual contributors as indicated
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
package org.jboss.ide.eclipse.as.ui.wizards;

import java.util.ArrayList;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.jboss.ide.eclipse.as.ui.Messages;

public class JBossRuntimeJava6WizardFragment extends JBossRuntimeWizardFragment {

	public JBossRuntimeJava6WizardFragment() {
	}
	
	protected boolean shouldIncludeDefaultJRE() {
		return false;
	}
	
	protected ArrayList<IVMInstall> getValidJREs() {
		ArrayList<IVMInstall> valid = new ArrayList<IVMInstall>();
		IVMInstallType[] vmInstallTypes = JavaRuntime.getVMInstallTypes();
		int size = vmInstallTypes.length;
		for (int i = 0; i < size; i++) {
			IVMInstall[] vmInstalls = vmInstallTypes[i].getVMInstalls();
			int size2 = vmInstalls.length;
			for (int j = 0; j < size2; j++) {
				if( vmInstalls[j] instanceof IVMInstall2 ) {
					String version = ((IVMInstall2)vmInstalls[j]).getJavaVersion();
					if( isValidVersion(version))
						valid.add(vmInstalls[j]);
				}
			}
		}
		return valid;
	}
	
	protected boolean isValidVersion(String version) {
		return !version.equals(JavaCore.VERSION_1_1) &&
				!version.equals(JavaCore.VERSION_1_2) &&
				!version.equals(JavaCore.VERSION_1_3) &&
				!version.equals(JavaCore.VERSION_1_4) &&			
				!version.equals(JavaCore.VERSION_1_5);
	}
	
	protected String getErrorString() {
		if( !shouldIncludeDefaultJRE() && getValidJREs().size() == 0 ) 
			return Messages.rwf_jre6NotFound;
		return super.getErrorString();
	}
}
