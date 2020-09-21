/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.core.server.internal.extendedproperties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.util.RuntimeUtils;
import org.jboss.tools.common.jdt.debug.JavaUtilities;

public class JBossEAP72DefaultLaunchArguments extends
JBossEAP70DefaultLaunchArguments {
	public JBossEAP72DefaultLaunchArguments(IServer s) {
		super(s);
	}
	public JBossEAP72DefaultLaunchArguments(IRuntime rt) {
		super(rt);
	}
	
	public static String getJava9VMArgsConditional(IRuntime rt) {
		if( isVmJava9(rt)) 
			return getJava9VMArgsDefault();
		return "";
	}
	public static boolean isVmJava9(IRuntime rt) {
		try {
			IJBossServerRuntime jbossRuntime = RuntimeUtils.checkedGetJBossServerRuntime(rt);
			IVMInstall vmInstall = jbossRuntime.getVM();
			int[] versionIDs = JavaUtilities.getMajorMinor(JavaUtilities.getJavaVersionVMInstall(vmInstall));
			if (versionIDs.length > 0 && versionIDs[0] >= 9) {
				return true;
			}
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
		return false;
	}
	
	public static String getJava9VMArgsDefault() {
		return " --add-exports=java.base/sun.nio.ch=ALL-UNNAMED --add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED --add-exports=jdk.unsupported/sun.reflect=ALL-UNNAMED --add-modules=java.se";
	}
	
	protected String getJava9VMArgs() {
		return getJava9VMArgsConditional(getRuntime());
	}
	
	@Override
	public String getStartDefaultVMArgs() {
		return super.getStartDefaultVMArgs() + getJava9VMArgs();
	}
}
