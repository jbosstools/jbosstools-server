package org.jboss.ide.eclipse.archives.jdt.integration.model;

import org.eclipse.core.runtime.IPath;
import org.jboss.ide.eclipse.archives.core.model.IArchiveModelRootNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNodeDelta;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbLibFileSet;

public class DeltaLibFileset extends ArchiveLibFileSetImpl {
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