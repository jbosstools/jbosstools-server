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

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.ui.internal.command.ServerCommand;
/**
 * @since 2.3
 */
public class ServerWorkingCopyPropertyButtonCommand extends ServerCommand {
	protected boolean oldVal;
	protected boolean newVal;
	protected String key;
	protected Button button;
	protected SelectionListener listener;
	protected IServerWorkingCopy wc;
	
	public ServerWorkingCopyPropertyButtonCommand(IServerWorkingCopy wc, String commandName, 
			Button button, boolean newVal, String attributeKey, SelectionListener listener) {
		super(wc, commandName);
		this.wc = wc;
		this.button = button;
		this.key = attributeKey;
		this.newVal = newVal;
		this.listener = listener;
		if( key != null )
			this.oldVal = wc.getAttribute(attributeKey, false); 
	}
	
	public void execute() {
		if( key != null )
			wc.setAttribute(key, newVal);
	}
	
	public void undo() {
		if( listener != null )
			button.removeSelectionListener(listener);
		if( key != null )
			wc.setAttribute(key, oldVal);
		if( button != null && !button.isDisposed())
			button.setSelection(oldVal);
		if( listener != null )
			button.addSelectionListener(listener);
	}
}