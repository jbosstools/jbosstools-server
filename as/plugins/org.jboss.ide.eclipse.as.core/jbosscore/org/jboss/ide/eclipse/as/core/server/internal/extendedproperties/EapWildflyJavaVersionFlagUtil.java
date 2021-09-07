/******************************************************************************* 
 * Copyright (c) 2021 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.internal.extendedproperties;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.wst.server.core.IRuntime;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.util.RuntimeUtils;
import org.jboss.tools.common.jdt.debug.JavaUtilities;

public class EapWildflyJavaVersionFlagUtil {
	public static boolean isVmJava9(IRuntime rt) {
		return isVmGTE(rt, 9);
	}

	public static boolean isVmJava16(IRuntime rt) {
		return isVmGTE(rt, 16);
	}

	public static boolean isVmGTE(IRuntime rt, int javaMajor) {
		try {
			IJBossServerRuntime jbossRuntime = RuntimeUtils.checkedGetJBossServerRuntime(rt);
			IVMInstall vmInstall = jbossRuntime.getVM();
			int[] versionIDs = JavaUtilities.getMajorMinor(JavaUtilities.getJavaVersionVMInstall(vmInstall));
			if (versionIDs.length > 0 && versionIDs[0] >= javaMajor) {
				return true;
			}
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
		return false;
	}

	public static String getJavaVersionVMArgs(IRuntime rt) {
		
		if( EapWildflyJavaVersionFlagUtil.isVmJava16(rt)) 
			return getJava16VMArgsDefault();
		if( EapWildflyJavaVersionFlagUtil.isVmJava9(rt)) 
			return getJava9VMArgsDefault();
		return "";
	}

	public static String getJava16VMArgsDefault() {
        final ArrayList<String> modularJavaOpts = new ArrayList<>();
        // Additions to these should include good explanations why in the relevant JIRA
        // Keep them alphabetical to avoid the code history getting confused by reordering commits
        modularJavaOpts.add("--add-exports=java.desktop/sun.awt=ALL-UNNAMED");
        modularJavaOpts.add("--add-exports=java.naming/com.sun.jndi.ldap=ALL-UNNAMED");
        modularJavaOpts.add("--add-opens=java.base/java.lang=ALL-UNNAMED");
        modularJavaOpts.add("--add-opens=java.base/java.lang.invoke=ALL-UNNAMED");
        modularJavaOpts.add("--add-opens=java.base/java.lang.reflect=ALL-UNNAMED");
        modularJavaOpts.add("--add-opens=java.base/java.io=ALL-UNNAMED");
        modularJavaOpts.add("--add-opens=java.base/java.security=ALL-UNNAMED");
        modularJavaOpts.add("--add-opens=java.base/java.util=ALL-UNNAMED");
        modularJavaOpts.add("--add-opens=java.base/java.util.concurrent=ALL-UNNAMED");
        modularJavaOpts.add("--add-opens=java.management/javax.management=ALL-UNNAMED");
        modularJavaOpts.add("--add-opens=java.naming/javax.naming=ALL-UNNAMED");
        modularJavaOpts.add("--add-exports=java.base/sun.nio.ch=ALL-UNNAMED");
        modularJavaOpts.add("--add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED");
        modularJavaOpts.add("--add-exports=jdk.unsupported/sun.reflect=ALL-UNNAMED");
        modularJavaOpts.add("--add-modules=java.se");
		return " " + String.join(" ", modularJavaOpts);
	}
	
	public static String getJava9VMArgsDefault() {
        final ArrayList<String> modularJavaOpts = new ArrayList<>();
        // Additions to these should include good explanations why in the relevant JIRA
        // Keep them alphabetical to avoid the code history getting confused by reordering commits
        modularJavaOpts.add("--add-exports=java.base/sun.nio.ch=ALL-UNNAMED");
        modularJavaOpts.add("--add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED");
        modularJavaOpts.add("--add-exports=jdk.unsupported/sun.reflect=ALL-UNNAMED");
        modularJavaOpts.add("--add-modules=java.se");
		return " " + String.join(" ", modularJavaOpts);
	}
	
}
