/******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.publishers;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IModule;
import org.jboss.ide.eclipse.as.core.server.IPublishCopyCallbackHandler;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.ide.eclipse.as.core.util.IConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.IWTPConstants;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IFilesystemController;

/**
 * This class is mostly legacy code for as < 7, and used in use cases
 * where a module must be forcibly restarted via the filesystem only.
 * This typically means touching the descriptor file for that module.
 * This class has become obsolete once modules no longer required
 * descriptors, and since as7 provides other mechanisms for restarting modules.
 * 
 * There is an API concern in the edits made here. 
 * IDescriptorToucher was technically public (though should nto have been)
 */
public class JSTPublisherXMLToucher {
	public static JSTPublisherXMLToucher instance;
	public static JSTPublisherXMLToucher getInstance() {
		if( instance == null ) 
			instance = new JSTPublisherXMLToucher();
		return instance;
	}
	
	private interface IDescriptorToucher {
		@Deprecated
		public void touchDescriptors(IPath moduleRoot, IPublishCopyCallbackHandler handler);
		public void touchDescriptors(IPath moduleRoot, IFilesystemController controller) throws CoreException;	
	}

	private static class PathDescriptorToucher implements IDescriptorToucher {
		private IPath path;
		// Takes relative paths
		public PathDescriptorToucher(String s) {
			this.path = (s == null ? null : new Path(s));
		}
		@Deprecated
		public void touchDescriptors(IPath moduleRoot, IPublishCopyCallbackHandler handler) {
			handler.touchResource(path, new NullProgressMonitor());
		}
		public void touchDescriptors(IPath moduleRoot, IFilesystemController controller) throws CoreException {
			// We should only touch xml files that *already* exist here
			// We don't want to create a random empty application.xml for example
			IPath toTouch = moduleRoot.append(path);
			if( controller.isFile(toTouch, new NullProgressMonitor())) {
				controller.touchResource(toTouch, new NullProgressMonitor());				
			}
		}
	}
 
	private HashMap<String, IDescriptorToucher> map;
	
	/**
	 * Constructor limited
	 * This constructor will add default touchers, but any new project type should
	 * add their own custom behaviour or path. 
	 */
	JSTPublisherXMLToucher() {
		// I know this is ugly but I don't care. it works. 
		map = new HashMap<String, IDescriptorToucher>();
		map.put(IWTPConstants.FACET_WEB, new PathDescriptorToucher(IJBossRuntimeResourceConstants.DESCRIPTOR_WEB));
		map.put(IWTPConstants.FACET_EJB, new PathDescriptorToucher(IJBossRuntimeResourceConstants.DESCRIPTOR_EJB));
		map.put(IWTPConstants.FACET_EAR, new PathDescriptorToucher(IJBossRuntimeResourceConstants.DESCRIPTOR_EAR));
		map.put(IWTPConstants.FACET_APP_CLIENT, new PathDescriptorToucher(IJBossRuntimeResourceConstants.DESCRIPTOR_CLIENT));
		map.put(IWTPConstants.FACET_CONNECTOR, new PathDescriptorToucher(IJBossRuntimeResourceConstants.DESCRIPTOR_CONNECTOR));
	}
	
	public void addDescriptorToucher(String typeId, IDescriptorToucher toucher) {
		map.put(typeId, toucher);
	}
	
	@Deprecated
	public void touch(IPath root, IModule module, IPublishCopyCallbackHandler handler) {
		String id = module.getModuleType().getId();
		IDescriptorToucher toucher = map.get(id);
		if( toucher == null )
			defaultTouch(root);
		else
			toucher.touchDescriptors(root, handler);
	}
	
	public void touch(IPath root, IModule module, IFilesystemController controller) throws CoreException {
		String id = module.getModuleType().getId();
		IDescriptorToucher toucher = map.get(id);
		toucher.touchDescriptors(root, controller);
	}
	
	// Touch all XML if we don't know what we're doing
	// This method doesn't even use the callback handler! it's using local!
	@Deprecated
	protected void defaultTouch(IPath deployPath) {
		// adjust timestamps
		FileFilter filter = new FileFilter() {
			public boolean accept(File pathname) {
				if( pathname.getAbsolutePath().toLowerCase().endsWith(IConstants.EXT_XML))
					return true;
				return false;
			}
		};
		FileUtil.touch(filter, deployPath.toFile(), true);
	}
}
