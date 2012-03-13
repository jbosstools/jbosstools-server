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
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.ui.internal.command.ServerCommand;
/**
 * @since 2.3
 */
public class ServerWorkingCopyPropertyCommand extends ServerCommand {
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
	}
	
	public void undo() {
		if( listener != null )
			text.removeModifyListener(listener);
		wc.setAttribute(key, oldVal);
		if( text != null && !text.isDisposed())
			text.setText(oldVal);
		if( listener != null )
			text.addModifyListener(listener);
	}
}