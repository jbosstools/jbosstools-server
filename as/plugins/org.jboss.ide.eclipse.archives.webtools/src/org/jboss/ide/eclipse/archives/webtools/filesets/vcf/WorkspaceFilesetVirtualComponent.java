/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.archives.webtools.filesets.vcf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
import org.jboss.ide.eclipse.archives.core.model.DirectoryScannerFactory;
import org.jboss.ide.eclipse.archives.core.model.DirectoryScannerFactory.DirectoryScannerExtension;
import org.jboss.ide.eclipse.archives.core.model.IArchiveModelRootNode;
import org.jboss.ide.eclipse.as.wtp.core.util.ResourceFilter;
import org.jboss.ide.eclipse.as.wtp.core.util.ResourceListVirtualFolder;
import org.jboss.ide.eclipse.as.wtp.core.vcf.AbstractFilesetVirtualComponent;

public class WorkspaceFilesetVirtualComponent extends AbstractFilesetVirtualComponent {
	private String rootFolderPath, includes, excludes;
	public WorkspaceFilesetVirtualComponent(IProject p, 
			IVirtualComponent referencingComponent, String rootPath) {
		super(p, referencingComponent);
		this.rootFolderPath = rootPath;
		includes = "**"; //$NON-NLS-1$
		excludes = ""; //$NON-NLS-1$
	}

	protected String getFirstIdSegment() {
		return FilesetComponentResolver.FILESET_PROTOCOL;
	}
	
	@Override
	public String getId() {
		return getFirstIdSegment();
	}

	public IVirtualFolder getRootFolder() {
		ResourceListVirtualFolder folder = (ResourceListVirtualFolder)super.getRootFolder();
		folder.setFilter(new WorkspaceFilter());
		return folder;
	}


	protected IResource[] getLooseResources() {
		return new IResource[]{};
	}

	protected IContainer[] getUnderlyingContainers() {
		if( rootFolderPath != null ) {
			IPath p = new Path(rootFolderPath);
			IContainer c = p.segmentCount() > 1 ? 
					ResourcesPlugin.getWorkspace().getRoot().getFolder(p) 
					: ResourcesPlugin.getWorkspace().getRoot().getProject(p.segment(0));
			return new IContainer[] { c };
		}
		return new IContainer[] { };
	}

	public void setRootFolderPath(String rootFolder) {
		this.rootFolderPath = rootFolder;
	}

	public String getRootFolderPath() {
		return rootFolderPath;
	}

	public void setIncludes(String includes) {
		this.includes = includes;
	}

	public String getIncludes() {
		return includes;
	}

	public void setExcludes(String excludes) {
		this.excludes = excludes;
	}

	public String getExcludes() {
		return excludes;
	}

	public class WorkspaceFilter implements ResourceFilter {
		private DirectoryScannerExtension scanner;
		public WorkspaceFilter() {
			scanner = DirectoryScannerFactory.createDirectoryScanner(
					rootFolderPath, new Path(""),  //$NON-NLS-1$
					includes, excludes, getProject().getName(), true,
					IArchiveModelRootNode.DESCRIPTOR_VERSION_1_3, true);
		}
		public boolean accepts(IResource resource) {
			String absolutePath = resource.getLocation().toFile().getAbsolutePath();
			ArrayList<DirectoryScannerExtension.FileWrapper> sum = getAllMatches(absolutePath);
			if( sum.size() > 0 ) {
				DirectoryScannerExtension.FileWrapper tmp;
				Iterator<DirectoryScannerExtension.FileWrapper> i = sum.iterator();
				while(i.hasNext()) {
					tmp = i.next();
					if( tmp.getWrapperPath().equals(resource.getFullPath()))
						return true;
				}
			}
			return false;
		}
		private ArrayList<DirectoryScannerExtension.FileWrapper> getAllMatches(String absolutePath) {
			HashMap<String, ArrayList<DirectoryScannerExtension.FileWrapper>> matchedFiles = scanner.getMatchedMap();
			HashMap<String, ArrayList<DirectoryScannerExtension.FileWrapper>> requiredFolders = scanner.getRequiredFolderMap();
			ArrayList<DirectoryScannerExtension.FileWrapper> sum = new ArrayList<DirectoryScannerExtension.FileWrapper>();
			
			if( matchedFiles.get(absolutePath) != null )
				sum.addAll(matchedFiles.get(absolutePath));
			if( requiredFolders.get(absolutePath) != null )
				sum.addAll(requiredFolders.get(absolutePath));
			return sum;
		}
	}
	
}
