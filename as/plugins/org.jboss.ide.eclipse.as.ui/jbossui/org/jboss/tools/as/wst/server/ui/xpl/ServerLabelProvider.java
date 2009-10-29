/*******************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Base Code
 *     Red Hat - Refactor for CNF
 *******************************************************************************/
package org.jboss.tools.as.wst.server.ui.xpl;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.ServerUICore;
import org.eclipse.wst.server.ui.internal.ImageResource;
import org.eclipse.wst.server.ui.internal.Messages;
import org.eclipse.wst.server.ui.internal.view.servers.ModuleServer;
/**
 * Server table label provider.
 * @deprecated
 */
public class ServerLabelProvider extends LabelProvider {

	/**
	 * ServerTableLabelProvider constructor comment.
	 */
	public ServerLabelProvider() {
		super();
	}

	public String getText(Object element) {
		if (element instanceof ModuleServer) {
			ModuleServer ms = (ModuleServer) element;
			if (ms.module == null)
				return ""; //$NON-NLS-1$
			int size = ms.module.length;
			String name = ms.module[size - 1].getName();
			return name;
		}
		
		if( element instanceof IServer ) {
			IServer server = (IServer) element;
			return notNull(server.getName());
		} 
		
		if( element == ServerContentProvider.INITIALIZING)
			return Messages.viewInitializing;

		return null;
	}
	public Image getImage(Object element) {
		Image image = null;
		if (element instanceof ModuleServer) {
			ModuleServer ms = (ModuleServer) element;
			ILabelProvider labelProvider = ServerUICore.getLabelProvider();
			image = labelProvider.getImage(ms.module[ms.module.length - 1]);
			labelProvider.dispose();
		} else if( element instanceof IServer ) {
			IServer server = (IServer) element;
			if (server.getServerType() != null) {
				image = ImageResource.getImage(server.getServerType().getId());
			}
		}
		return image;
	}

	protected String notNull(String s) {
		if (s == null)
			return ""; //$NON-NLS-1$
		return s;
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}
}