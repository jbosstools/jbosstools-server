/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.ui.editor;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.ui.editor.ServerEditorPart;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;
import org.eclipse.wst.server.ui.internal.editor.ServerEditorPartInput;
import org.eclipse.wst.server.ui.internal.editor.ServerResourceCommandManager;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.jboss.ide.eclipse.as.ui.editor.IDeploymentTypeUI.IServerModeUICallback;

/**
 * Provide utility methods to acquire a valid callback for
 * use in either a new server wizard or inside a server editor
 * 
 * @author rob.stryker
 *
 */
public class DeploymentTypeUIUtil {

	public interface ICompletable {
		public void setComplete(boolean complete);
	}
	
	public static IServerModeUICallback getCallback(TaskModel tm, IWizardHandle handle, ICompletable completable) {
		return new NewServerWizardBehaviourCallback(tm, handle, completable);
	}

	public static IServerModeUICallback getCallback(final IServerWorkingCopy server, IEditorInput input, ServerEditorPart part) {
		return new ServerEditorUICallback(server, input, part);
	}
	public static IServerModeUICallback getCallback(final IServerWorkingCopy server, IEditorInput input, ServerEditorSection section) {
		return new ServerEditorUICallback(server, input, section);
	}

	
	/**
	 * For use inside a wizard fragment
	 */
	public static class NewServerWizardBehaviourCallback implements IServerModeUICallback {
		protected TaskModel tm;
		protected IWizardHandle handle;
		protected ICompletable completable;
		public NewServerWizardBehaviourCallback(TaskModel tm, 
				IWizardHandle handle, ICompletable completable) {
			this.tm = tm;
			this.handle = handle;
			this.completable = completable;
		}
		public IRuntime getRuntime() {
			return (IRuntime) tm.getObject(TaskModel.TASK_RUNTIME);
		}
		public IServerWorkingCopy getServer() {
			return (IServerWorkingCopy) tm.getObject(TaskModel.TASK_SERVER);
		}
		public IWizardHandle getHandle() {
			return handle;
		}
		public void execute(IUndoableOperation operation) {
			try {
				operation.execute(new NullProgressMonitor(), null);
			} catch(ExecutionException  ee) {
				// TODO
			}
		}
		public void executeLongRunning(Job j) {
			// depends on COMMON, DAMN
//			IWizardContainer container = ((WizardPage)handle).getWizard().getContainer();
//			try {
//				WizardUtils.runInWizard(j, null, container);
//			} catch(Exception e) {
//				// TODO clean
//			}
			j.schedule();
		}
		public void setErrorMessage(String msg) {
			if( completable != null )
				completable.setComplete(msg == null);
			handle.setMessage(msg, IMessageProvider.ERROR);
			handle.update();
		}
		public Object getAttribute(String key) {
			return tm.getObject(key);
		}
	}

	
	/**
	 * For use inside a server editor
	 */
	public static class ServerEditorUICallback implements IServerModeUICallback {
		private IServerWorkingCopy server;
		private ServerResourceCommandManager commandManager;
		private ServerEditorPart part;
		private ServerEditorSection section;
		public ServerEditorUICallback(final IServerWorkingCopy server, IEditorInput input, ServerEditorPart part ) {
			this.part = part;
			this.server = server;
			commandManager = ((ServerEditorPartInput) input).getServerCommandManager();
		}
		public ServerEditorUICallback(final IServerWorkingCopy server, IEditorInput input, ServerEditorSection section) {
			this.section = section;
			this.server = server;
			commandManager = ((ServerEditorPartInput) input).getServerCommandManager();
		}
		public IServerWorkingCopy getServer() {
			return server;
		}
		public void execute(IUndoableOperation operation) {
			commandManager.execute(operation);
		}
		public IRuntime getRuntime() {
			return server.getRuntime();
		}
		public void executeLongRunning(Job j) {
			j.schedule();
		}
		public void setErrorMessage(String msg) {
			if( part != null )
				part.setErrorMessage(msg);
			else
				section.setErrorMessage(msg);
		}
		public Object getAttribute(String key) {
			return null;
		}
	}

}
