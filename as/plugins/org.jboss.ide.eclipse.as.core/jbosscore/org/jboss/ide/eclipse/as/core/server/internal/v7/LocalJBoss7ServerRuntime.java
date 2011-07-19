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
package org.jboss.ide.eclipse.as.core.server.internal.v7;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jboss.ide.eclipse.as.core.server.internal.LocalJBossServerRuntime;

public class LocalJBoss7ServerRuntime extends LocalJBossServerRuntime {

	@Override
	public IStatus validate() {
		return Status.OK_STATUS;
	}

	@Override
	public String getDefaultRunArgs(IPath serverHome) {
		return "-mp \"" //$NON-NLS-1$
				+ serverHome.append("modules").toString() //$NON-NLS-1$ 
				+ "\" -logmodule org.jboss.logmanager -jaxpmodule javax.xml.jaxp-provider org.jboss.as.standalone"; //$NON-NLS-1$
	}
		
	@Override
	public String getDefaultRunVMArgs(IPath serverHome) {
		IPath bootLog = serverHome.append("standalone").append("log").append("boot.log"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		IPath logConfig = serverHome.append("standalone").append("configuration").append("logging.properties"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		return "-server -Xms64m -Xmx512m -XX:MaxPermSize=256m " //$NON-NLS-1$
				+ "-Djava.net.preferIPv4Stack=true " //$NON-NLS-1$
				+ "-Dorg.jboss.resolver.warning=true "  //$NON-NLS-1$
				+ "-Dsun.rmi.dgc.client.gcInterval=3600000 " //$NON-NLS-1$
				+ "-Dsun.rmi.dgc.server.gcInterval=3600000 " //$NON-NLS-1$
				+ "\"-Dorg.jboss.boot.log.file=" + bootLog.toString() + "\" " //$NON-NLS-1$ //$NON-NLS-2$
				+ "\"-Dlogging.configuration=file:" + logConfig.toString() + "\" " //$NON-NLS-1$ //$NON-NLS-2$ 
				+ "\"-Djboss.home.dir=" + serverHome.toString() + "\""; //$NON-NLS-1$ //$NON-NLS-2$"
	}

	@Override
	public String getDefaultRunVMArgs() {
		IPath loc = getRuntime().getLocation();
		IPath bootLog = loc.append("standalone").append("log").append("boot.log"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		IPath logConfig = loc.append("standalone").append("configuration").append("logging.properties"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		return "-server -Xms64m -Xmx512m -XX:MaxPermSize=256m " //$NON-NLS-1$
				+ "-Djava.net.preferIPv4Stack=true " //$NON-NLS-1$
				+ "-Dorg.jboss.resolver.warning=true "  //$NON-NLS-1$
				+ "-Dsun.rmi.dgc.client.gcInterval=3600000 " //$NON-NLS-1$
				+ "-Dsun.rmi.dgc.server.gcInterval=3600000 " //$NON-NLS-1$
				+ "\"-Dorg.jboss.boot.log.file=" + bootLog.toString() + "\" " //$NON-NLS-1$ //$NON-NLS-2$
				+ "\"-Dlogging.configuration=file:" + logConfig.toString() + "\" " //$NON-NLS-1$ //$NON-NLS-2$ 
				+ "\"-Djboss.home.dir=" + loc.toString() + "\""; //$NON-NLS-1$ //$NON-NLS-2$"
	}
}
