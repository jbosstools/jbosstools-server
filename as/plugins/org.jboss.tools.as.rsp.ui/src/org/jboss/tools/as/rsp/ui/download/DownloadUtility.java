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
import java.io.InputStream;
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
	    	BufferedOutputStream destination = new BufferedOutputStream(
					new FileOutputStream(dlFilePath.toString()));
			IStatus result = new URLTransportUtility().download(dlFilePath.toString(),
					url, destination, 30000, progressIndicator);
			if( !result.isOK() ) {
				RspUiActivator.pluginLog().logStatus(result);
			}
		} catch(IOException ioe) {
			RspUiActivator.pluginLog().logError(ioe);
		}
    }

    public void uncompress(Path dlFilePath, Path destinationFolder) throws IOException {
        //new UnzipUtility(dlFilePath.toFile()).extract(destinationFolder.toFile());
    }


    private static void downloadFile(InputStream input, Path dlFileName,
                                     IProgressMonitor progressIndicator, long size) throws IOException {
//        byte[] buffer = new byte[4096];
//        Files.createDirectories(dlFileName.getParent());
//        progressIndicator.beginTask("Downloading " + dlFileName.toFile().getName(), (int)size);
//        try (OutputStream output = Files.newOutputStream(dlFileName)) {
//            int lg;
//            long accumulated = 0;
//            while (((lg = input.read(buffer)) > 0) && !progressIndicator.isCanceled()) {
//                output.write(buffer, 0, lg);
//                accumulated += lg;
//                progressIndicator.worked(lg);
//            }
//        }
    }
}
