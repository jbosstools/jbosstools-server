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
package org.jboss.ide.eclipse.archives.core.model.other.internal;

import java.util.HashMap;

import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNodeDelta;
import org.jboss.ide.eclipse.archives.core.model.internal.ArchiveDeltaPreNodeFactory;
import org.jboss.ide.eclipse.archives.core.model.internal.ArchiveNodeFactory;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbPackageNode;

public class WorkspaceNodeFactory extends ArchiveNodeFactory {
	public IArchiveNode createNode(XbPackageNode node) {
		IArchiveNode sNode = super.createNode(node);
		if( sNode == null ) {
			return createNodeInternal(node);
		}
		return sNode;
	}
	
	protected IArchiveNode createNodeInternal(XbPackageNode node) {
		WorkspaceExtensionManager manager = 
			(WorkspaceExtensionManager)ArchivesCore.getInstance().getExtensionManager();
		INodeProvider[] providers = manager.getNodeProviders();
		for( int i = 0; i < providers.length; i++ )
			if( providers[i].canCreateNode(node))
				return providers[i].createNode(node);
		return null;
	}
	
	public IArchiveNode createDeltaNode(IArchiveNodeDelta parentDelta, IArchiveNode postChange, 
			HashMap attributeChanges, HashMap propertyChanges) {
		IArchiveNode node = new ArchiveDeltaPreNodeFactory().
					createNode(parentDelta, postChange, attributeChanges, propertyChanges);
		if( node == null ) {
			WorkspaceExtensionManager manager = 
				(WorkspaceExtensionManager)ArchivesCore.getInstance().getExtensionManager();
			INodeProvider[] providers = manager.getNodeProviders();
			for( int i = 0; i < providers.length; i++ )
				if( providers[i].canCreateDelta(postChange))
					return providers[i].createDelta(parentDelta, postChange, attributeChanges, propertyChanges);
		}
		return node;
	}
}
