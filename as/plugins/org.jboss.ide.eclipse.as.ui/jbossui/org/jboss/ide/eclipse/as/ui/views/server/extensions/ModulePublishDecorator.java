/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.ui.views.server.extensions;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.IServerModule;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;
import org.jboss.ide.eclipse.as.wtp.core.server.publish.ModulePublishErrorCache;

public class ModulePublishDecorator implements ILightweightLabelDecorator {

	public void addListener(ILabelProviderListener listener) {
	}
	public void dispose() {
	}
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}
	public void removeListener(ILabelProviderListener listener) {
	}
	public void decorate(Object element, IDecoration decoration) {
		IStatus ret = recentPublishStatus(element);
		if( ret != null ) {
			int sev = ret.getSeverity();
			if( sev == IStatus.ERROR) {
				ImageDescriptor id = JBossServerUISharedImages.getImageDescriptor(JBossServerUISharedImages.ERROR_MARKER);
				decoration.addOverlay(id, IDecoration.BOTTOM_LEFT);
			}
		}
	}
	
	protected IStatus recentPublishStatus(Object element) {
		if( element instanceof IServerModule ) {
			IServer s = ((IServerModule)element).getServer();
			IModule[] m = ((IServerModule)element).getModule();
			if( s == null )
				return null;
			
			IStatus status = ModulePublishErrorCache.getDefault().getPublishErrorStatus(s, m);
			return status;
		}
		return Status.OK_STATUS;
	}
}
