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
import java.util.Date;
import java.util.HashMap;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IModule;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.ide.eclipse.as.core.util.IConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.IWTPConstants;

public class JSTPublisherXMLToucher {
	public static JSTPublisherXMLToucher instance;
	public static JSTPublisherXMLToucher getInstance() {
		if( instance == null ) 
			instance = new JSTPublisherXMLToucher();
		return instance;
	}
	
	public interface IDescriptorToucher {
		public void touchDescriptors(IPath moduleRoot);
	}
	
	public static class PathDescriptorToucher implements IDescriptorToucher {
		private IPath[] paths;
		// Takes relative paths
		public PathDescriptorToucher(String s) {
			this(new Path(s));
		}
		public PathDescriptorToucher(IPath p) {
			this(new IPath[]{p});
		}
		public PathDescriptorToucher(IPath[] path) {
			this.paths = path == null ? new IPath[0] : path;
		}
		public void touchDescriptors(IPath moduleRoot) {
			File tmp;
			for( int i = 0; i < paths.length; i++ ) {
				tmp = moduleRoot.append(paths[i]).toFile();
				if( tmp.exists())
					tmp.setLastModified(new Date().getTime());
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
		IJBossRuntimeResourceConstants i = new IJBossRuntimeResourceConstants(){};
		map.put(IWTPConstants.FACET_WEB, new PathDescriptorToucher(i.DESCRIPTOR_WEB));
		map.put(IWTPConstants.FACET_EJB, new PathDescriptorToucher(i.DESCRIPTOR_EJB));
		map.put(IWTPConstants.FACET_EAR, new PathDescriptorToucher(i.DESCRIPTOR_EAR));
		map.put(IWTPConstants.FACET_APP_CLIENT, new PathDescriptorToucher(i.DESCRIPTOR_CLIENT));
		map.put(IWTPConstants.FACET_CONNECTOR, new PathDescriptorToucher(i.DESCRIPTOR_CONNECTOR));
	}
	
	public void addDescriptorToucher(String typeId, IDescriptorToucher toucher) {
		map.put(typeId, toucher);
	}
	
	public void touch(IPath root, IModule module) {
		String id = module.getModuleType().getId();
		IDescriptorToucher toucher = map.get(id);
		if( toucher == null )
			defaultTouch(root);
		else
			toucher.touchDescriptors(root);
	}
	
	
	// Touch all XML if we don't know what we're doing
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
