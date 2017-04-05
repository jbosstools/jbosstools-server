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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
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
	protected ModifyListener modlistener;
	protected SelectionListener selListener;
	protected IServerWorkingCopy wc;
	
	public ServerWorkingCopyPropertyComboCommand(IServerWorkingCopy wc, String commandName, 
			Combo combo, String newVal, String attributeKey, ModifyListener listener) {
		super(wc, commandName);
		this.wc = wc;
		this.combo = combo;
		this.key = attributeKey;
		this.newVal = newVal;
		this.modlistener = listener;
		if( key != null )
			this.oldVal = wc.getAttribute(attributeKey, ""); //$NON-NLS-1$
	}
	
	public ServerWorkingCopyPropertyComboCommand(IServerWorkingCopy wc, String commandName, 
			Combo combo, String newVal, String attributeKey, SelectionListener listener) {
		super(wc, commandName);
		this.wc = wc;
		this.combo = combo;
		this.key = attributeKey;
		this.newVal = newVal;
		this.selListener = listener;
		if( key != null )
			this.oldVal = wc.getAttribute(attributeKey, ""); //$NON-NLS-1$
	}
	
	@Override
	public void execute() {
		wc.setAttribute(key, newVal);
		postOp(POST_EXECUTE);
	}
	
	private void addListeners() {
		if( combo != null && !combo.isDisposed()) {
			if( modlistener != null )
				combo.addModifyListener(modlistener);
			if( selListener != null )
				combo.addSelectionListener(selListener);
		}
	}

	private void removeListeners() {
		if( combo != null && !combo.isDisposed()) {
			if( modlistener != null )
				combo.removeModifyListener(modlistener);
			if( selListener != null )
				combo.removeSelectionListener(selListener);
		}
	}

	@Override
	public void undo() {
		removeListeners();
		wc.setAttribute(key, oldVal);
		if( combo != null && !combo.isDisposed()) {
			select(oldVal);
		}
		addListeners();
		postOp(POST_UNDO);
	}

	private void select(String val) {
		String oldValAsText = getStringForValue(oldVal);
		int toSel = getItemIndex(oldValAsText);
		if( toSel != -1 ) {
			combo.select(toSel);
		} else {
			if( (combo.getStyle() & SWT.READ_ONLY) > 0) {
				combo.deselectAll();
			} else {
				combo.setText(oldValAsText);
			}
		}
	}
	
	protected int getItemIndex(String text) {
		String[] items = combo.getItems();
		for( int i = 0; i < items.length; i++ ) {
			if( items[i].equals(text))
				return i;
		}
		return -1;
	}
	
	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable adapt) {
		removeListeners();
		wc.setAttribute(key, newVal);
		if( combo != null && !combo.isDisposed()) {
			select(newVal);
		}
		addListeners();
		postOp(POST_REDO);
		return Status.OK_STATUS;
	}

	protected void postOp(int type) {
		// Do Nothing
	}
	
	
	/**
	 * If you're using a combo with user visible strings different from stored values, this
	 * method will assist in setting the combo text properly
	 * @return
	 */
	protected String getStringForValue(String value) {
		return value;
	}
}