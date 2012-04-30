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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.ui.internal.command.ServerCommand;
/**
 * @since 2.3
 */
public class ServerWorkingCopyPropertyComboCommand extends ServerCommand {
	public static int POST_EXECUTE = 1;
	public static int POST_UNDO = 2;
	public static int POST_REDO = 3;
	protected String oldVal;
	protected String newVal;
	protected String key;
	protected Combo combo;
	protected ModifyListener listener;
	protected IServerWorkingCopy wc;
	
	public ServerWorkingCopyPropertyComboCommand(IServerWorkingCopy wc, String commandName, 
			Combo combo, String newVal, String attributeKey, ModifyListener listener) {
		super(wc, commandName);
		this.wc = wc;
		this.combo = combo;
		this.key = attributeKey;
		this.newVal = newVal;
		this.listener = listener;
		if( key != null )
			this.oldVal = wc.getAttribute(attributeKey, ""); //$NON-NLS-1$
	}
	
	public void execute() {
		wc.setAttribute(key, newVal);
		postOp(POST_EXECUTE);
	}
	
	public void undo() {
		if( listener != null )
			combo.removeModifyListener(listener);
		wc.setAttribute(key, oldVal);
		if( combo != null && !combo.isDisposed())
			combo.setText(oldVal);
		if( listener != null )
			combo.addModifyListener(listener);
		postOp(POST_UNDO);
	}

	public IStatus redo() {
		if( listener != null )
			combo.removeModifyListener(listener);
		wc.setAttribute(key, newVal);
		if( combo != null && !combo.isDisposed())
			combo.setText(newVal);
		if( listener != null )
			combo.addModifyListener(listener);
		postOp(POST_REDO);
		return Status.OK_STATUS;
	}
	protected void postOp(int type) {
		// Do Nothing
	}
}