/******************************************************************************* 
 * Copyright (c) 2015 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.tools.jmx.ui.internal.actions;

import java.util.HashMap;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreePath;
import org.jboss.tools.jmx.core.IConnectionWrapper;

public class RefreshActionState {
	private static RefreshActionState model;
	public static RefreshActionState getDefault() {
		if( model == null )
			model = new RefreshActionState();
		return model;
	}
	
	private HashMap<IConnectionWrapper, ISelection> selection = new HashMap<IConnectionWrapper, ISelection>();
	private HashMap<IConnectionWrapper, TreePath[]> expansion = new HashMap<IConnectionWrapper, TreePath[]>();
	
	
	public void setSelection(IConnectionWrapper con, ISelection sel) {
		selection.put(con,  sel);
	}
	public void setExpansion(IConnectionWrapper con, TreePath[] expansion) {
		this.expansion.put(con,  expansion);
	}
	
	public ISelection getSelection(IConnectionWrapper w) {
		return selection.get(w);
	}
	
	public TreePath[] getExpansion(IConnectionWrapper w) {
		return expansion.get(w);
	}
}
