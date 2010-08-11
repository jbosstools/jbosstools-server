/*******************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - Initial Implementation
 *******************************************************************************/
package org.jboss.tools.as.wst.server.ui.xpl;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.navigator.CommonDropAdapter;
import org.eclipse.ui.navigator.CommonDropAdapterAssistant;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.IModuleType;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.ui.internal.EclipseUtil;
import org.eclipse.wst.server.ui.internal.ServerUIPlugin;
import org.eclipse.wst.server.ui.internal.actions.RunOnServerActionDelegate;

/**
 * 
 * @author rob
 * @deprecated
 */
public class ServersViewDropAdapterAssistant extends CommonDropAdapterAssistant {

	@SuppressWarnings("unchecked") private List fElements;

	public ServersViewDropAdapterAssistant() {
		super();
	}
	protected void doInit() {
	}

	public IStatus validatePluginTransferDrop(
			IStructuredSelection aDragSelection, Object aDropTarget) {
		initializeSelection(aDragSelection);
		return internalValidate(aDropTarget, fElements);
	}

	public IStatus validateDrop(Object target, int operation,
			TransferData transferType) {
		if (LocalSelectionTransfer.getInstance().isSupportedType(transferType)) {
			ISelection s = LocalSelectionTransfer.getInstance().getSelection();
			initializeSelection(s);
			return internalValidate(target, fElements);
		}
		return Status.CANCEL_STATUS;
	}

	protected void initializeSelection(ISelection s) {
		if (fElements != null)
			return;
		if (!(s instanceof IStructuredSelection)) {
			fElements= Collections.EMPTY_LIST;
			return;
		}
		fElements = ((IStructuredSelection) s).toList();
	}
	
	@SuppressWarnings("unchecked")
	protected IStatus internalValidate(Object target, List elements) {
		if( target instanceof IServer ) {
			IServer server = (IServer)target;
			Object next;
			if( elements != null ) {
				Iterator i = elements.iterator();
				while(i.hasNext() ) {
					next = i.next();
					IModuleArtifact[] moduleArtifacts = ServerPlugin.getModuleArtifacts(next);
					if (moduleArtifacts != null && moduleArtifacts.length > 0 ) {
						for( int j = 0; j < moduleArtifacts.length; j++ ) {
							if( moduleArtifacts[j] != null && moduleArtifacts[j].getModule() != null ) {
								IModuleType[] moduleTypes = server.getServerType().getRuntimeType().getModuleTypes();
								if (ServerUtil.isSupportedModule(moduleTypes, moduleArtifacts[j].getModule().getModuleType())) {
									return Status.OK_STATUS;
								}
							}
						}
					}
				}
			}
		}
		clear();
		return Status.CANCEL_STATUS;
	}

	public IStatus handleDrop(CommonDropAdapter dropAdapter,
			DropTargetEvent dropTargetEvent, Object target) {
		IStatus ret = internalHandleDrop(target, fElements);
		if( ret.isOK())
			dropTargetEvent.detail = DND.DROP_NONE;
		return ret;
	}
	
	public IStatus handlePluginTransferDrop(
			IStructuredSelection aDragSelection, Object aDropTarget) {
		return internalHandleDrop(aDropTarget, fElements);
	}

	protected IStatus internalHandleDrop(Object target, List elements) {
		boolean b = false;
		if( target instanceof IServer ) {
			b = true;
			if( fElements != null ) {
				Iterator iterator = elements.iterator();
				while (iterator.hasNext()) {
					Object data2 = iterator.next();
					if (!doSel((IServer)target, data2))
						b = false;
				}
			}
		}
		clear();
		return b ? Status.OK_STATUS : Status.CANCEL_STATUS;
	}
	
	private void clear() {
		fElements = null;
	}

	protected boolean doSel(IServer server, Object data) {
		// check if the selection is a project (module) that we can add to the server
		IProject project = (IProject) Platform.getAdapterManager().getAdapter(data, IProject.class);
		if (project != null) {
			IModule[] modules = ServerUtil.getModules(project);
			if (modules != null && modules.length == 1) {
				try {
					IServerWorkingCopy wc = server.createWorkingCopy();
					IModule[] parents = wc.getRootModules(modules[0], null);
					if (parents == null || parents.length == 0)
						return false;
					
					if (ServerUtil.containsModule(server, parents[0], null))
						return false;
					
					IModule[] add = new IModule[] { parents[0] };
					if (wc.canModifyModules(add, null, null).getSeverity() != IStatus.ERROR) {
						wc.modifyModules(modules, null, null);
						wc.save(false, null);
						return true;
					}
				} catch (final CoreException ce) {
					final Shell shell = Workbench.getInstance().getActiveWorkbenchWindow().getShell(); 
					shell.getDisplay().asyncExec(new Runnable() {
						public void run() {
							EclipseUtil.openError(shell, ce.getLocalizedMessage());
						}
					});
					return true;
				}
			}
		}
		
		// otherwise, try Run on Server
		final IServer finalServer = server;
		RunOnServerActionDelegate ros = new RunOnServerActionDelegate() {
			public IServer getServer(IModule module, IModuleArtifact moduleArtifact, IProgressMonitor monitor) throws CoreException {
				if (!ServerUIPlugin.isCompatibleWithLaunchMode(finalServer, launchMode))
					return null;
				
				if (!ServerUtil.containsModule(finalServer, module, monitor)) {
					IServerWorkingCopy wc = finalServer.createWorkingCopy();
					try {
						ServerUtil.modifyModules(wc, new IModule[] { module }, new IModule[0], monitor);
						wc.save(false, monitor);
					} catch (CoreException ce) {
						throw ce;
					}
				}
				
				return finalServer;
			}
		};
		Action action = new Action() {
			//
		};
		ros.selectionChanged(action, new StructuredSelection(data));
		
		ros.run(action);
		return true;
	}
}
