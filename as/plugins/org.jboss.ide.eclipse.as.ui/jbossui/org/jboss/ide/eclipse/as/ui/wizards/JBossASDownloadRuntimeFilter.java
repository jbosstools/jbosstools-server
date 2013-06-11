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
package org.jboss.ide.eclipse.as.ui.wizards;

import java.util.Arrays;

import org.eclipse.wst.server.core.IRuntimeType;
import org.jboss.tools.as.runtimes.integration.util.DownloadRuntimeServerUtil;
import org.jboss.tools.runtime.core.model.DownloadRuntime;
import org.jboss.tools.runtime.core.model.IDownloadRuntimeFilter;

public class JBossASDownloadRuntimeFilter implements IDownloadRuntimeFilter {
	private IRuntimeType runtimeType;
	public JBossASDownloadRuntimeFilter(IRuntimeType rtType) {
		this.runtimeType = rtType;
	}
	public boolean accepts(DownloadRuntime runtime) {
		DownloadRuntime[] all = DownloadRuntimeServerUtil.getDownloadRuntimes(runtimeType);
		return Arrays.asList(all).contains(runtime);
	}
	public DownloadRuntime[] filter(DownloadRuntime[] runtimes) {
		return DownloadRuntimeServerUtil.getDownloadRuntimes(runtimeType, runtimes);
	}
}
