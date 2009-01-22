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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.ui.ServerUICore;
import org.eclipse.wst.server.ui.internal.ImageResource;
import org.eclipse.wst.server.ui.internal.Messages;
import org.eclipse.wst.server.ui.internal.ServerUIPlugin;
import org.eclipse.wst.server.ui.internal.provisional.UIDecoratorManager;
import org.eclipse.wst.server.ui.internal.view.servers.ModuleServer;

public class ServerDecorator extends LabelProvider implements ILightweightLabelDecorator {

	public static final String[] syncState = new String[] {
		Messages.viewSyncOkay,
		Messages.viewSyncRestart,
		Messages.viewSyncPublish,
		Messages.viewSyncRestartPublish,
		Messages.viewSyncPublishing};

	public static final String[] syncStateUnmanaged = new String[] {
		Messages.viewSyncOkay2,
		Messages.viewSyncRestart2,
		Messages.viewSyncPublish2,
		Messages.viewSyncRestartPublish2,
		Messages.viewSyncPublishing2};

	public static final String[] modulePublishState = new String[] {
		"",
		Messages.viewSyncOkay,
		Messages.viewSyncPublish,
		Messages.viewSyncPublish};

	private static ServerDecorator instance;
	public static ServerDecorator getDefault() {
		return instance;
	}

	private static int count = 0;
	public static void animate() {
		count = (count + 1)%3;
	}
	public static int getCount() {
		return count;
	}
	
	public ServerDecorator() {
		instance = this;
	}

	public void decorate(Object element, IDecoration decoration) {
		if( element instanceof IServer ) {
			String state = getServerStateLabel((IServer)element);
			String status = getServerStatusLabel((IServer)element);
			decoration.addSuffix(combine(state, status));
		} else if( element instanceof ModuleServer ) {
			String state = getModuleStateText((ModuleServer)element);
			String status = getModuleStatusText((ModuleServer)element);
			decoration.addSuffix(combine(state, status));
		}
	}
	
	protected String combine(String state, String status) {
		if(isEmpty(state) && isEmpty(status))
			return "";
		if( isEmpty(state))
			return "  [" + status + "]";
		if( isEmpty(status))
			return "  [" + state + "]";
		return "  [" + state + ", " + status + "]";
	}
	
	protected boolean isEmpty(String s) {
		return (s == null || "".equals(s));
	}
	
	public void redecorate(IServer server) {
		fireLabelProviderChanged(new LabelProviderChangedEvent(this));
	}

	
	
	/*
	 * Utility methods
	 */
	public static Image getServerImage(IServer server) {
		return server == null ? null : 
			server.getServerType() == null ? null : 
				ImageResource.getImage(server.getServerType().getId());
	}
	
	public static String getServerStateLabel(IServer server) {
		return server == null ? null : 
			server.getServerType() == null ? null : 
				getStateLabel(server.getServerType(), server.getServerState(), server.getMode());
	}

	public static String getStateLabel(IServerType serverType, int state, String mode) {
		return serverType == null ? null : 
			UIDecoratorManager.getUIDecorator(serverType).getStateLabel(state, mode, count);
	}
	
	public static String getServerStatusLabel(IServer server) {
		IStatus status = ((Server) server).getServerStatus();
		if (status != null)
			return status.getMessage();
		
		if (server.getServerType() == null)
			return "";

		if (server.getServerState() == IServer.STATE_UNKNOWN)
			return "";
		
		String serverId = server.getId();
		if (ServerContentProvider.publishing.contains(serverId))
			return ServerDecorator.syncState[4];
		
		// republish
		int i = 0;
		if (server.shouldPublish()) {
			if (((Server)server).isPublishUnknown())
				return "";
			i += 2;
		}
		
		if (server.shouldRestart())
			i = 1;
		
		return syncState[i];
	}

	public static Image getServerStateImage(IServer server) {
		return server == null ? null : 
			getStateImage(server.getServerType(), server.getServerState(), server.getMode());
	}

	public static Image getStateImage(IServerType serverType, int state, String mode) {
		return serverType == null ? null : 
			UIDecoratorManager.getUIDecorator(serverType).getStateImage(state, mode, getCount());
	}
	
	public static String getModuleText(ModuleServer ms ) { 
		if (ms == null || ms.module == null)
			return "";
		int size = ms.module.length;
		return ms.module[size - 1].getName();
	}
	
	public static Image getModuleImage(ModuleServer ms) {
		if( ms != null ) {
			ILabelProvider labelProvider = ServerUICore.getLabelProvider();
			Image image = labelProvider.getImage(ms.module[ms.module.length - 1]);
			labelProvider.dispose();
			return image;
		} 
		return null;
	}
	
	public static String getModuleStateText(ModuleServer ms) {
		return "";
	}
	
	public static String getModuleStatusText(ModuleServer ms) {
		if( ms != null && ms.server != null && ms.module != null ) {
			IStatus status = ((Server) ms.server).getModuleStatus(ms.module);
			if (status != null)
				return status.getMessage();
			
			return modulePublishState[ms.server.getModulePublishState(ms.module)];
		}
		return "";
	}
	
	public static Image getModuleStatusImage(ModuleServer ms) {
		IStatus status = ((Server) ms.server).getModuleStatus(ms.module);
		if (status != null) {
			ISharedImages sharedImages = ServerUIPlugin.getInstance().getWorkbench().getSharedImages();
			if (status.getSeverity() == IStatus.ERROR)
				return sharedImages.getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
			else if (status.getSeverity() == IStatus.WARNING)
				return sharedImages.getImage(ISharedImages.IMG_OBJS_WARN_TSK);
			else if (status.getSeverity() == IStatus.INFO)
				return sharedImages.getImage(ISharedImages.IMG_OBJS_INFO_TSK);
		}
		return null;
	}
}
