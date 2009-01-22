/*******************************************************************************
 * Copyright (c) 2003, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Code taken from Superclass
 *     Red Hat Inc - Refactor
 *******************************************************************************/
package org.jboss.tools.as.wst.server.ui.xpl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.navigator.CommonDragAdapter;
import org.eclipse.ui.navigator.CommonDropAdapter;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
/**
 * A view of servers, their modules, and status.
 */
public class ServersView extends CommonNavigator {
	
	/**
	 * ServersView constructor comment.
	 */
	public ServersView() {
		super();
	}
	
	/*
	 *  Stuff that shouldn't even be here but CNF is kinda lame
	 *  Must override currently to overcome the bug below: 
	 *  
	 *  https://bugs.eclipse.org/bugs/show_bug.cgi?id=261606
	 */
	private IMemento memento;
	
	public void init(IViewSite aSite, IMemento aMemento)
	throws PartInitException {
		this.memento = memento;
		super.init(aSite, aMemento);
	}
	
	protected CommonViewer createCommonViewer(Composite aParent) {
		CommonViewer aViewer = new CommonViewerExtension(getViewSite().getId(), aParent,
				SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		initListeners(aViewer);
		aViewer.getNavigatorContentService().restoreState(memento);
		return aViewer;
	}

	
	public class CommonViewerExtension extends CommonViewer {
		public CommonViewerExtension(String aViewerId, Composite aParent, int aStyle) {
			super(aViewerId, aParent, aStyle);
		}
		
		protected void initDragAndDrop() {
			/* Handle Drag and Drop */
			int operations = DND.DROP_COPY | DND.DROP_MOVE;
	
			CommonDragAdapter dragAdapter = new CommonDragAdapter(
					getNavigatorContentService(), this);
			addDragSupport(operations, dragAdapter.getSupportedDragTransfers(),
					dragAdapter);
	
			ServersDropAdapter dropAdapter = new ServersDropAdapter(
					getNavigatorContentService(), this);
			addDropSupport(operations, dropAdapter.getSupportedDropTransfers(),
					dropAdapter);
		}
	}
}