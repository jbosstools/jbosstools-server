/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.rsp.ui.util.ui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.jboss.tools.as.rsp.ui.RspUiActivator;
import org.jboss.tools.foundation.core.plugin.log.StatusFactory;

/**
 * Utilities related to opening editors
 */
public class EditorUtil {
	public static IEditorPart openFileInEditor(File f) {
		IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(f.getAbsoluteFile().getParent()));
		fileStore = fileStore.getChild(f.getName());
		IFileInfo fetchInfo = fileStore.fetchInfo();
		if (!fetchInfo.isDirectory() && fetchInfo.exists()) {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			try {
				final IEditorPart part = IDE.openEditorOnFileStore(page, fileStore);
				return part;
			} catch (PartInitException pie) {
				RspUiActivator.log(StatusFactory.errorStatus(RspUiActivator.PLUGIN_ID,
						"Unable to open file in editor: " + f.getAbsolutePath(), pie));
			}
		}
		return null;
	}

	public static IEditorPart createAndOpenVirtualFile(String name, String content) {
		try {
			File vf = createTempFile(name, content);
			if (vf != null) {
				vf.deleteOnExit();
				return openFileInEditor(vf);
			}
		} catch (IOException e) {
			RspUiActivator.log(
					StatusFactory.errorStatus(RspUiActivator.PLUGIN_ID, "Unable to open virtual file in editor", e));
		}
		return null;
	}

	public static File createTempFile(String name, String content) throws IOException {
		File file = new File(System.getProperty("java.io.tmpdir"), name);
		if (file.exists()) {
			file.delete();
		}
		Files.write(file.toPath(), content.getBytes());
		return file;
	}
}
