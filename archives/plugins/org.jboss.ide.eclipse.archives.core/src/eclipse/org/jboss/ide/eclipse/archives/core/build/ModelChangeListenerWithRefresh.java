/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.archives.core.build;


import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.jboss.ide.eclipse.archives.core.ArchivesCoreMessages;
import org.jboss.ide.eclipse.archives.core.ArchivesCorePlugin;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveModel;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNodeDelta;

/**
 * This class responds to model change events.
 * It is given a delta as to what nodes are added, removed, or changed.
 * It then keeps the output file for the top level archive in sync with
 * the changes to the model.
 *
 * If the automatic builder is not enabled for this project, the listener
 * does nothing.
 *
 * @author Rob Stryker (rob.stryker@redhat.com)
 *
 */
public class ModelChangeListenerWithRefresh extends ModelChangeListener {
	protected void executeAndLog(IArchiveNodeDelta delta) {
		final IArchiveNodeDelta delta2 = delta;
		
		new Job(ArchivesCoreMessages.UpdatingModelJob) {
			public IStatus run(IProgressMonitor monitor) {
				ModelChangeListenerWithRefresh.super.executeAndLog(delta2);
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	
	protected void postChange(IArchiveNode node) {
		IArchive pack = node.getRootArchive();
		if( pack != null ) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IContainer proj = root.getContainerForLocation(pack.getProjectPath());
			try {
				proj.setSessionProperty(new QualifiedName(ArchivesCorePlugin.PLUGIN_ID, "localname"), "inUse"); //$NON-NLS-1$ //$NON-NLS-2$
				if( pack.isDestinationInWorkspace() ) {
					// refresh the root package node
					IResource res = root.getContainerForLocation(pack.getProjectPath());
					if( res != null ) {
						try {
							// refresh infinitely in case the output is exploded
							res.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
						} catch( CoreException ce ) {
						}
					}
				}

				try {
					proj.getFile(new Path(IArchiveModel.DEFAULT_PACKAGES_FILE)).refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
				} catch( CoreException ce ) {
				}
			} catch( CoreException ce ) {
			}
		}
	}
}
