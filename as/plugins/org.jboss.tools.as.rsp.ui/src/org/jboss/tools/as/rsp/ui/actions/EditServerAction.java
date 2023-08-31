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
package org.jboss.tools.as.rsp.ui.actions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ExecutionException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.texteditor.ITextEditor;
import org.jboss.tools.as.rsp.ui.client.RspClientLauncher;
import org.jboss.tools.as.rsp.ui.internal.views.navigator.RSPContentProvider.ServerStateWrapper;
import org.jboss.tools.as.rsp.ui.model.impl.RspCore;
import org.jboss.tools.as.rsp.ui.util.ui.EditorUtil;
import org.jboss.tools.rsp.api.dao.CreateServerResponse;
import org.jboss.tools.rsp.api.dao.GetServerJsonResponse;
import org.jboss.tools.rsp.api.dao.UpdateServerRequest;
import org.jboss.tools.rsp.api.dao.UpdateServerResponse;

public class EditServerAction extends AbstractTreeAction {

	public EditServerAction(ISelectionProvider provider) {
		super(provider, org.jboss.tools.as.rsp.ui.actions.Messages.EditServerAction_0);
	}

	@Override
	protected boolean isVisible(Object[] o) {
		return safeSingleItemClass(o, ServerStateWrapper.class);
	}

	@Override
	protected boolean isEnabled(Object[] o) {
		return safeSingleItemClass(o, ServerStateWrapper.class);
	}

	@Override
	protected void singleSelectionActionPerformed(Object selected) {
		if (selected instanceof ServerStateWrapper) {
			ServerStateWrapper server = (ServerStateWrapper) selected;
			RspClientLauncher client = RspCore.getDefault().getClient(server.getRsp());
			String typeId = server.getServerState().getServer().getType().getId();
			try {
				GetServerJsonResponse response = client.getServerProxy()
						.getServerAsJson(server.getServerState().getServer()).get();
				// TelemetryService.instance().sendWithType(TelemetryService.TELEMETRY_SERVER_EDIT,
				// typeId, response.getStatus());
				if (response.getStatus() != null && !response.getStatus().isOK()) {
					showError(response.getStatus().getMessage(), org.jboss.tools.as.rsp.ui.actions.Messages.EditServerAction_1);
				} else {
					// OK assumed
					String fName = server.getServerState().getServer().getId() + ".json";
					File vf = EditorUtil.createTempFile(fName, response.getServerJson());
					openEditor(server, vf);
				}
			} catch (InterruptedException | ExecutionException | IOException e) {
				// TelemetryService.instance().sendWithType(TelemetryService.TELEMETRY_SERVER_EDIT,
				// typeId, interruptedException);
				showError(org.jboss.tools.as.rsp.ui.actions.Messages.EditServerAction_3 + e.getMessage(), org.jboss.tools.as.rsp.ui.actions.Messages.EditServerAction_4);
			}
		}
	}

	private String fileToString(File f) {
		String initialContents = null;
		try {
			byte[] arr = Files.readAllBytes(f.toPath());
			initialContents = new String(arr);
		} catch (IOException ioe) {
			return null;
		}
		final String initialContents2 = initialContents;
		return initialContents2;
	}

	private void openEditor(ServerStateWrapper server, File vf) {
		String initialContents = fileToString(vf);
		if (initialContents == null) {
			// TODO
			return;
		}
		final String initialContents2 = initialContents;
		IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(vf.getAbsoluteFile().getParent()));
		fileStore = fileStore.getChild(vf.getName());
		IFileInfo fetchInfo = fileStore.fetchInfo();
		if (!fetchInfo.isDirectory() && fetchInfo.exists()) {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			try {
				final IEditorPart part = IDE.openEditorOnFileStore(page, fileStore);
				if (part != null) {
					part.addPropertyListener(new IPropertyListener() {
						@Override
						public void propertyChanged(Object source, int propId) {
							if (propId == IWorkbenchPartConstants.PROP_DIRTY && !part.isDirty()) {
								IEditorInput input = part.getEditorInput();
								if ((part instanceof ITextEditor)) {
									ITextEditor ite = (ITextEditor) part;
									IDocument doc = ite.getDocumentProvider().getDocument(ite.getEditorInput());
									String contents = doc.get();
									if (contents != null && !contents.equals(initialContents2)) {
										persistChanges(server, part, contents);
									}
								}
							}
						}

					});
				}
			} catch (PartInitException e) {
				String msg = NLS.bind("", //$NON-NLS-1$
						fileStore.getName());
				IDEWorkbenchPlugin.log(msg, e.getStatus());
				MessageDialog.open(MessageDialog.ERROR, PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						"", msg, SWT.SHEET); //$NON-NLS-1$
			}
		}
	}

	private void persistChanges(ServerStateWrapper server, IEditorPart part, String contents) {
		RspClientLauncher client = RspCore.getDefault().getClient(server.getRsp());
		if (client != null) {
			new Thread(org.jboss.tools.as.rsp.ui.actions.Messages.EditServerAction_5 + server.getServerState().getServer().getId()) {
				public void run() {
					persistChanges(client, server, part, contents);
				}
			}.start();
		}
	}

	private void persistChanges(RspClientLauncher client, ServerStateWrapper server, IEditorPart part,
			String contents) {
		try {
			UpdateServerRequest req = new UpdateServerRequest();
			req.setHandle(server.getServerState().getServer());
			req.setServerJson(contents);
			UpdateServerResponse resp = client.getServerProxy().updateServer(req).get();
			CreateServerResponse response = resp.getValidation();
			// TelemetryService.instance().sendWithType(TelemetryService.TELEMETRY_SERVER_EDIT,
			// typeId, response.getStatus());
			if (response.getStatus() != null && !response.getStatus().isOK()) {
				showError(response.getStatus().getMessage(), org.jboss.tools.as.rsp.ui.actions.Messages.EditServerAction_6);
			} else {
//                // OK assumed
//                String fName = server.getServerState().getServer().getId() + ".json";
//                File vf = createTempFile(fName, response.getServerJson());
//                openEditor(server, vf);                    
			}
		} catch (InterruptedException | ExecutionException e) {
			// TelemetryService.instance().sendWithType(TelemetryService.TELEMETRY_SERVER_EDIT,
			// typeId, interruptedException);
			showError(org.jboss.tools.as.rsp.ui.actions.Messages.EditServerAction_7 + e.getMessage(), org.jboss.tools.as.rsp.ui.actions.Messages.EditServerAction_4);
		}
	}
}
