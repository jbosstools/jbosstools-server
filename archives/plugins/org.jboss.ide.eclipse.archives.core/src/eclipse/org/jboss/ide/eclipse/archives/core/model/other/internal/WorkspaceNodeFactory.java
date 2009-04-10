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

import org.eclipse.core.runtime.IPath;
import org.jboss.ide.eclipse.archives.core.model.IArchiveLibFileSet;
import org.jboss.ide.eclipse.archives.core.model.IArchiveModelRootNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNodeDelta;
import org.jboss.ide.eclipse.archives.core.model.internal.ArchiveDeltaPreNodeFactory;
import org.jboss.ide.eclipse.archives.core.model.internal.ArchiveNodeFactory;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbLibFileSet;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbPackageNode;

public class WorkspaceNodeFactory extends ArchiveNodeFactory {
	public ArchiveLibFileSetImpl createLibFileset() {
		return new ArchiveLibFileSetImpl();
	}
	public IArchiveNode createNode(XbPackageNode node) {
		IArchiveNode sNode = super.createNode(node);
		if( sNode == null ) {
			if (node instanceof XbLibFileSet) {
				return new ArchiveLibFileSetImpl((XbLibFileSet)node);
			}
		}
		return sNode;
	}
	public IArchiveNode createDeltaNode(IArchiveNodeDelta parentDelta, IArchiveNode postChange, 
			HashMap attributeChanges, HashMap propertyChanges) {
		return new WorkspaceArchiveDeltaPreNodeFactory().createNode(parentDelta, postChange, attributeChanges, propertyChanges);
	}
	
	public static class WorkspaceArchiveDeltaPreNodeFactory extends ArchiveDeltaPreNodeFactory {
		
		public IArchiveNode createNode(IArchiveNodeDelta parentDelta, IArchiveNode postChange, 
				HashMap attributeChanges, HashMap propertyChanges) {
			IArchiveNode superImpl = super.createNode(parentDelta, postChange, attributeChanges, propertyChanges);
			if( superImpl == null ) { 
				if( postChange instanceof ArchiveLibFileSetImpl ) {
					XbLibFileSet fs = createLibFileset((ArchiveLibFileSetImpl)postChange, attributeChanges, propertyChanges); 
					return new DeltaLibFileset(fs, parentDelta, postChange);
				}
			}
			return superImpl;
		}
		
		protected static XbLibFileSet createLibFileset(ArchiveLibFileSetImpl postChange,HashMap attributeChanges, HashMap propertyChanges ) {
			XbLibFileSet fs = new XbLibFileSet((XbLibFileSet)postChange.getNodeDelegate());
			if( attributeChanges.containsKey(IArchiveLibFileSet.ID_ATTRIBUTE))
				fs.setId(getBeforeString(attributeChanges, IArchiveLibFileSet.ID_ATTRIBUTE));
			undoPropertyChanges(fs, propertyChanges);
			return fs;
		}
	}
	
	public static class DeltaLibFileset extends ArchiveLibFileSetImpl {
		// everything goes through the delegate or the parent. Simple
		private IArchiveNodeDelta parentDelta; 
		private IArchiveNode impl;
		public DeltaLibFileset(XbLibFileSet fileset, IArchiveNodeDelta parentDelta, IArchiveNode impl){
			super(fileset);
			this.parentDelta = parentDelta;
			this.impl = impl;
		}
		public IArchiveNode getParent() {
			return parentDelta == null ? null : parentDelta.getPreNode();
		}
		public IPath getProjectPath() {
			return impl.getProjectPath();
		}
		public IArchiveModelRootNode getModelRootNode() {
			return impl.getModelRootNode();
		}
	}
}
