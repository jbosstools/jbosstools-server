/******************************************************************************* 
 * Copyright (c) 2009 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.wtp.core;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jst.j2ee.application.internal.operations.IModuleExtensions;
import org.eclipse.jst.j2ee.internal.common.classpath.J2EEComponentClasspathUpdater;
import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
import org.eclipse.jst.j2ee.project.EarUtilities;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.internal.builder.IDependencyGraph;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
import org.eclipse.wst.common.componentcore.resources.IVirtualResource;

/**
 * This file is here as a clone to J2EEDependencyListener, but that class
 * has bugs that will only be fixed in WTP 3.1.2 or WTP 3.2.0. 
 * 
 * This class MUST BE REMOVED at that point
 * @author rob
 *
 */

public class WTP31DependencyListener implements IResourceChangeListener,
		IResourceDeltaVisitor {

	public static WTP31DependencyListener INSTANCE = new WTP31DependencyListener();

	private WTP31DependencyListener() {
	}

	// private List<IProject> cachedEARModuleDependencies = new
	// ArrayList<IProject>();
	//
	// private void cacheModuleDependencies(IProject earProject) {
	// if (EarUtilities.isEARProject(earProject)) {
	// IVirtualReference[] refs =
	// EarUtilities.getComponentReferences(ComponentCore.createComponent(earProject));
	// IVirtualComponent comp = null;
	// for (int j = 0; j < refs.length; j++) {
	// comp = refs[j].getReferencedComponent();
	// if (!comp.isBinary()) {
	// cachedEARModuleDependencies.add(comp.getProject());
	// }
	// }
	// }
	// }
	//
	// private void updateModuleDependencies() {
	// if (!cachedEARModuleDependencies.isEmpty()) {
	// for (Iterator<IProject> iterator =
	// cachedEARModuleDependencies.iterator(); iterator.hasNext();) {
	// IDependencyGraph.INSTANCE.update(iterator.next());
	// }
	// cachedEARModuleDependencies.clear();
	// }
	// }

	public void resourceChanged(IResourceChangeEvent event) {
		try {
			IDependencyGraph.INSTANCE.preUpdate();
			switch (event.getType()) {
			case IResourceChangeEvent.PRE_CLOSE:
			case IResourceChangeEvent.PRE_DELETE:
				// IResource resource = event.getResource();
				// if (resource.getType() == IResource.PROJECT) {
				// cacheModuleDependencies((IProject) resource);
				// }
				break;
			case IResourceChangeEvent.POST_CHANGE:
				event.getDelta().accept(this);
			}
		} catch (CoreException e) {
			J2EEPlugin.logError(e);
		} finally {
			IDependencyGraph.INSTANCE.postUpdate();
		}
	}

	public boolean visit(IResourceDelta delta) throws CoreException {
		IResource resource = delta.getResource();
		switch (resource.getType()) {
		case IResource.ROOT:
			return true;
		case IResource.PROJECT:
			int kind = delta.getKind();
			// if ((IResourceDelta.ADDED & kind) != 0) {
			// // if an EAR project is added then all dependent modules must be
			// // updated
			// if (EarUtilities.isEARProject((IProject) resource)) {
			// cacheModuleDependencies((IProject) resource);
			// updateModuleDependencies();
			// }
			// return false;
			// } else if ((IResourceDelta.REMOVED & kind) != 0) {
			// updateModuleDependencies();
			// return false;
			/* } else */
			if ((IResourceDelta.CHANGED & kind) != 0) {
				// int flags = delta.getFlags();
				// if ((IResourceDelta.OPEN & flags) != 0) {
				// boolean isOpen = ((IProject) resource).isOpen();
				// if (isOpen) {
				// // if an EAR project is open all dependent modules must
				// // be updated
				// cacheModuleDependencies((IProject) resource);
				// }
				// // this will also pickup both close and open events
				// // if the EAR project is closed, the cached dependent
				// // modules will already
				// updateModuleDependencies();
				// }
				return true;
			}
			return false;
		case IResource.FOLDER:
			return true;
		case IResource.FILE:
			String name = resource.getName();
			// MANIFEST.MF must be all caps per spec
			// if (name.equals(J2EEConstants.MANIFEST_SHORT_NAME)) {
			// IFile manifestFile =
			// J2EEProjectUtilities.getManifestFile(resource.getProject(),
			// false);
			// if (null == manifestFile || resource.equals(manifestFile)) {
			// IDependencyGraph.INSTANCE.update(resource.getProject(),
			// IDependencyGraph.MODIFIED);
			// }
			// } else if
			// (name.equals(WTPModulesResourceFactory.WTP_MODULES_SHORT_NAME)) {
			// if (EarUtilities.isEARProject(resource.getProject())) {
			// // unfortunately, in this case, the only easy way to update
			// // is to force the update of all projects
			// IProject[] allProjects =
			// ResourcesPlugin.getWorkspace().getRoot().getProjects();
			// for (IProject sourceProject : allProjects) {
			// IDependencyGraph.INSTANCE.update(sourceProject,
			// IDependencyGraph.MODIFIED);
			// }
			// }
			/* } else */
			if (endsWithIgnoreCase(name, IModuleExtensions.DOT_JAR)
					|| endsWithIgnoreCase(name, IModuleExtensions.DOT_WAR)
					|| endsWithIgnoreCase(name, IModuleExtensions.DOT_RAR)) {
				if (EarUtilities.isEARProject(resource.getProject())) {
					IVirtualComponent comp = ComponentCore
							.createComponent(resource.getProject());
					// if (isFolder(resource.getParent(), comp.getRootFolder()))
					// {
					if (!isFolder(resource.getParent(), comp.getRootFolder())) {
						if( isInTree((IFile)resource, comp.getRootFolder())) {
							IVirtualReference[] refs = comp.getReferences();
							for (IVirtualReference ref : refs) {
								IDependencyGraph.INSTANCE.update(ref
										.getReferencedComponent().getProject(),
										IDependencyGraph.MODIFIED);
							}
							IDependencyGraph.INSTANCE.update(resource.getProject(),
									IDependencyGraph.MODIFIED);
						}
					}

				}
			}
		default:
			return false;
		}
	}

	public static boolean isInTree(IFile file, IVirtualFolder folder) {
		// If we are the folder, return true
		if (isFolder(file.getParent(), folder))
			return true;

		// if resource is any level under current VF's underlying folders,
		// return true
		IContainer[] underlying = folder.getUnderlyingFolders();
		for (int i = 0; i < underlying.length; i++) {
			if (underlying[i].getFullPath().isPrefixOf(file.getFullPath()))
				return true;
		}

		// continue to peruse in case there's some odd mapping, such as
		// /EarConten5 -> /my/secret/location/wherever/it/goes
		boolean found = false;
		try {
			IVirtualResource[] children = folder.members();
			for (int i = 0; i < children.length && !found; i++) {
				if (children[i].getType() == IVirtualResource.FOLDER) {
					found |= isInTree(file, (IVirtualFolder) children[i]);
				}
			}
		} catch (CoreException ce) {
			J2EEPlugin.logError(ce);
		}
		return found;
	}

	public static boolean endsWithIgnoreCase(String str, String sfx) {
		return J2EEComponentClasspathUpdater.endsWithIgnoreCase(str, sfx);

	}

	public static boolean isFolder(IResource resource, IVirtualFolder folder) {
		return J2EEComponentClasspathUpdater.isFolder(resource, folder);
	}
}
