/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.runtimes.integration.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.tools.as.runtimes.integration.internal.DownloadRuntimesProvider;
import org.jboss.tools.runtime.core.RuntimeCoreActivator;
import org.jboss.tools.runtime.core.model.DownloadRuntime;

/**
 * Easily discover mappings for a download runtime to a wtp runtime
 */
public class DownloadRuntimeServerUtil {
	
	public static IRuntimeType getWTPRuntime(DownloadRuntime rt) {
		String rtType = (String)rt.getProperty(DownloadRuntimesProvider.PROP_WTP_RUNTIME);
		if( rtType != null ) {
			return ServerCore.findRuntimeType(rtType);
		}
		return null;
	}
	
	public static DownloadRuntime[] getDownloadRuntimes(IRuntimeType type) {
		Collection<DownloadRuntime> dlRuntimes = RuntimeCoreActivator.getDefault().getDownloadRuntimes().values();
		DownloadRuntime[] asArr = (DownloadRuntime[]) dlRuntimes.toArray(new DownloadRuntime[dlRuntimes.size()]);
		return getDownloadRuntimes(type, asArr);
	}
	
	public static DownloadRuntime[] getDownloadRuntimes(IRuntimeType type, Map<String, DownloadRuntime> dlRuntimes) {
		Collection<DownloadRuntime> dlRuntimes2 = dlRuntimes.values();
		DownloadRuntime[] asArr = (DownloadRuntime[]) dlRuntimes2.toArray(new DownloadRuntime[dlRuntimes2.size()]);
		return getDownloadRuntimes(type, asArr);
	}
	
	public static DownloadRuntime[] getDownloadRuntimes(IRuntimeType type, DownloadRuntime[] dlRuntimes) {
		if( type == null )
			return null;
		ArrayList<DownloadRuntime> retval = new ArrayList<DownloadRuntime>();
		for( int i = 0; i < dlRuntimes.length; i++ ) {
			DownloadRuntime rt = dlRuntimes[i];
			if( type.equals(getWTPRuntime(rt)))
					retval.add(rt);
		}
		return (DownloadRuntime[]) retval.toArray(new DownloadRuntime[retval.size()]);
	}
}