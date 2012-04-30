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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.ui.internal.command.ServerCommand;
/**
 * @since 2.3
 */
public class ServerWorkingCopyPropertyCommand extends ServerCommand {
	public static int POST_EXECUTE = 1;
	public static int POST_UNDO = 2;
	public static int POST_REDO = 3;
	
	protected String oldVal;
	protected String newVal;
	protected String key;
	protected Text text;
	protected ModifyListener listener;
	protected IServerWorkingCopy wc;
	
	public ServerWorkingCopyPropertyCommand(IServerWorkingCopy wc, String commandName, 
			Text text, String newVal, String attributeKey, ModifyListener listener) {
		this(wc, commandName, text, newVal, attributeKey, listener, "");
	}
	public ServerWorkingCopyPropertyCommand(IServerWorkingCopy wc, String commandName, 
				Text text, String newVal, String attributeKey, ModifyListener listener, String defaultVal) {
		super(wc, commandName);
		this.wc = wc;
		this.text = text;
		this.key = attributeKey;
		this.newVal = newVal;
		this.listener = listener;
		if( key != null )
			this.oldVal = wc.getAttribute(attributeKey, defaultVal);
	}
	
	public void execute() {
		wc.setAttribute(key, newVal);
		postOp(POST_EXECUTE);
	}
	
	public void undo() {
		if( listener != null )
			text.removeModifyListener(listener);
		wc.setAttribute(key, oldVal);
		if( text != null && !text.isDisposed())
			text.setText(oldVal);
		if( listener != null )
			text.addModifyListener(listener);
		postOp(POST_UNDO);
	}
	public IStatus redo(IProgressMonitor monitor, IAdaptable adapt) {
		if( listener != null )
			text.removeModifyListener(listener);
		wc.setAttribute(key, newVal);
		if( text != null && !text.isDisposed())
			text.setText(newVal);
		if( listener != null )
			text.addModifyListener(listener);
		postOp(POST_REDO);
		return Status.OK_STATUS;
	}
	
	protected void postOp(int type) {
		// Do Nothing
	}
}