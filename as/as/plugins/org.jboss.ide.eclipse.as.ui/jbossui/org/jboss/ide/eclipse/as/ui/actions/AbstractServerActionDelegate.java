/*******************************************************************************
 * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.ui.actions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public abstract class AbstractServerActionDelegate implements IWorkbenchWindowActionDelegate, ServerManagerListener {
	
	static Map<String, AbstractServerActionDelegate> delegates = new HashMap<String,AbstractServerActionDelegate>();

	protected IWorkbenchWindow window;
	protected IAction action;
	protected boolean isInitialised = false;
	
	public AbstractServerActionDelegate() {
		delegates.put(getClass().getName(), this);
	}
	
	public static void updateAll() {
		AbstractServerActionDelegate[] ds = delegates.values().toArray(new AbstractServerActionDelegate[0]);
		for (int i = 0; i < ds.length; i++) {
			try {
				ds[i].update();
			} catch (Exception e) {
			}
		}		
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				initModel();
			}
		});
	}
	
	private void initModel() {
		ServerManager.getInstance().addListener(this);
		update();
		isInitialised = true;
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
		if(this.action == action) return;
		this.action = action;
		serverManagerChanged();
	}
	
	public void run(IAction action) {
		if(!isInitialised) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if(isActionEnabled()) {
						_run();
					}
				}
			});
		} else {
			_run();
		}
	}
	
	private void _run() {
		try {
			doRun();
		} catch (Exception e) {
		}
	}
		
	public void dispose() {}
	protected void doRun() {}
	
	protected boolean isActionEnabled() {
		return true;
	}
	
	protected void update() {
		if(action != null) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					action.setEnabled(isActionEnabled());
					setToolTip();
				}
			});
		} 
	}
	
	public void serverManagerChanged() {
		update();
	}
	
	protected void setToolTip() {
		if(action != null) {
			String tooltip = computeToolTip();
			if(tooltip != null) action.setToolTipText(tooltip);
		}
	}
	
	protected String computeToolTip() {
		return null;
	}	

	
	// a stub that can be used by subclasses
	protected static ISelectionProvider getSelectionProvider() {
		return new ISelectionProvider() {
			public void addSelectionChangedListener(
					ISelectionChangedListener listener) {
			}
			public ISelection getSelection() {
				return null;
			}
			public void removeSelectionChangedListener(
					ISelectionChangedListener listener) {
			}
			public void setSelection(ISelection selection) {
			}
		};
	}
	
}
