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
package org.jboss.ide.eclipse.as.core.runtime;

import java.util.Map;

import org.eclipse.wst.server.core.IRuntimeType;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.tools.as.runtimes.integration.util.DownloadRuntimeServerUtil;
import org.jboss.tools.runtime.core.model.DownloadRuntime;

/**
 * This class is internal but its replacement is public api. 
 * Please use org.jboss.tools.as.runtimes.integration.util.DownloadRuntimeServerUtil
 * @deprecated
 */
public class DownloadRuntimeToWTPRuntime implements IJBossToolingConstants {

	public static IRuntimeType getWTPRuntime(DownloadRuntime rt) {
		return DownloadRuntimeServerUtil.getWTPRuntime(rt);
	}
	
	public static DownloadRuntime[] getDownloadRuntimes(IRuntimeType type) {
		return DownloadRuntimeServerUtil.getDownloadRuntimes(type);
	}
	
	public static DownloadRuntime[] getDownloadRuntimes(IRuntimeType type, DownloadRuntime[] dlRuntimes) {
		return DownloadRuntimeServerUtil.getDownloadRuntimes(type, dlRuntimes);
	}
	
	public static DownloadRuntime[] getDownloadRuntimes(IRuntimeType type, Map<String, DownloadRuntime> dlRuntimes) {
		return DownloadRuntimeServerUtil.getDownloadRuntimes(type, dlRuntimes);
	}
}
