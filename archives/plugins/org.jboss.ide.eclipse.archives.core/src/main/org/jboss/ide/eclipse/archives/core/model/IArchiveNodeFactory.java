package org.jboss.ide.eclipse.archives.core.model;

import java.util.HashMap;

import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbPackageNode;


public interface IArchiveNodeFactory {
	public IArchive createArchive();
	public IArchiveStandardFileSet createFileset();
	public IArchiveFolder createFolder();
	public IArchiveAction createAction();
	public IArchiveNode createNode(XbPackageNode node);
	public IArchiveNode createDeltaNode(IArchiveNodeDelta parentDelta, IArchiveNode postChange, 
			HashMap attributeChanges, HashMap propertyChanges);
}
