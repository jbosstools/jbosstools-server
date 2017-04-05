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
package org.jboss.ide.eclipse.as.wtp.ui.editor;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.ui.internal.command.ServerCommand;
/**
 * @since 2.3
 */
public class ServerWorkingCopyPropertyButtonCommand extends ServerCommand {
	public static int POST_EXECUTE = 1;
	public static int POST_UNDO = 2;
	public static int POST_REDO = 3;
	protected boolean oldVal;
	protected boolean newVal;
	protected String key;
	protected Button button;
	protected SelectionListener listener;
	protected IServerWorkingCopy wc;
	
	public ServerWorkingCopyPropertyButtonCommand(IServerWorkingCopy wc, String commandName, 
			Button button, boolean newVal, String attributeKey, SelectionListener listener) {
		this(wc, commandName, button, newVal, attributeKey, listener, false);
	}
	public ServerWorkingCopyPropertyButtonCommand(IServerWorkingCopy wc, String commandName, 
			Button button, boolean newVal, String attributeKey, SelectionListener listener, boolean defaultval) {
		super(wc, commandName);
		this.wc = wc;
		this.button = button;
		this.key = attributeKey;
		this.newVal = newVal;
		this.listener = listener;
		if( key != null )
			this.oldVal = wc.getAttribute(attributeKey, defaultval); 
	}
	
	@Override
	public void execute() {
		if( key != null )
			wc.setAttribute(key, newVal);
		postOp(POST_EXECUTE);
	}
	
	@Override
	public void undo() {
		toggle(oldVal);
		postOp(POST_UNDO);
	}
	
	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable adapt) {
		toggle(newVal);
		return Status.OK_STATUS;
	}
	
	private void toggle(boolean val) {
		if( listener != null )
			button.removeSelectionListener(listener);
		if( key != null )
			wc.setAttribute(key, val);
		if( button != null && !button.isDisposed())
			button.setSelection(val);
		if( listener != null )
			button.addSelectionListener(listener);
		postOp(POST_REDO);
	}
	
	protected void postOp(int type) {
		// Do Nothing
	}
}