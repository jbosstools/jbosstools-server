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

import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.navigator.CommonNavigator;
/**
 * A view of servers, their modules, and status.
 * @deprecated
 */
public class ServersView extends CommonNavigator {
	
	/**
	 * ServersView constructor comment.
	 */
	public ServersView() {
		super();
	}
	
	private IMemento memento;
	
	public void init(IViewSite aSite, IMemento aMemento)
	throws PartInitException {
		this.memento = memento;
		super.init(aSite, aMemento);
	}
}