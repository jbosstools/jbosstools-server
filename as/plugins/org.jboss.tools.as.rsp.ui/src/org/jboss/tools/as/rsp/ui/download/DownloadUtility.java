/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.rsp.ui.download;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.jboss.tools.as.rsp.ui.RspUiActivator;
import org.jboss.tools.foundation.core.ecf.URLTransportUtility;

/**
 * Download a remote file
 */
public class DownloadUtility {

	public void download(String url, Path dlFilePath, IProgressMonitor progressIndicator) throws IOException {
		try {
			BufferedOutputStream destination = new BufferedOutputStream(new FileOutputStream(dlFilePath.toString()));
			IStatus result = new URLTransportUtility().download(dlFilePath.toString(), url, destination, 30000,
					progressIndicator);
			if (!result.isOK()) {
				RspUiActivator.pluginLog().logStatus(result);
			}
		} catch (IOException ioe) {
			RspUiActivator.pluginLog().logError(ioe);
		}
	}
}
