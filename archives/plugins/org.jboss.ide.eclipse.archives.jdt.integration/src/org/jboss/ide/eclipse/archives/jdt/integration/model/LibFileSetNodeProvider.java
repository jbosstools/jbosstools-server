package org.jboss.ide.eclipse.archives.jdt.integration.model;

import java.util.HashMap;

import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNodeDelta;
import org.jboss.ide.eclipse.archives.core.model.internal.ArchiveDeltaPreNodeFactory;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbLibFileSet;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbPackageNode;
import org.jboss.ide.eclipse.archives.core.model.other.internal.INodeProvider;

public class LibFileSetNodeProvider implements INodeProvider {

	public boolean canCreateNode(XbPackageNode node) {
		return (node instanceof XbLibFileSet);
	}

	public IArchiveNode createNode(XbPackageNode node) {
		if (node instanceof XbLibFileSet) {
			return new ArchiveLibFileSetImpl((XbLibFileSet)node);
		}
		return null;
	}

	public boolean canCreateDelta(IArchiveNode node) {
		return (node instanceof ArchiveLibFileSetImpl);
	}

	public IArchiveNode createDelta(IArchiveNodeDelta parentDelta,
			IArchiveNode postChange, HashMap attributeChanges,
			HashMap propertyChanges) {
		if( postChange instanceof ArchiveLibFileSetImpl ) {
			XbLibFileSet fs = createLibFileset((ArchiveLibFileSetImpl)postChange, attributeChanges, propertyChanges); 
			return new DeltaLibFileset(fs, parentDelta, postChange);
		}
		return null;
	}
	
	protected static XbLibFileSet createLibFileset(ArchiveLibFileSetImpl postChange,HashMap attributeChanges, HashMap propertyChanges ) {
		XbLibFileSet fs = new XbLibFileSet((XbLibFileSet)postChange.getNodeDelegate());
		if( attributeChanges.containsKey(IArchiveLibFileSet.ID_ATTRIBUTE))
			fs.setId(ArchiveDeltaPreNodeFactory.getBeforeString(attributeChanges, IArchiveLibFileSet.ID_ATTRIBUTE));
		ArchiveDeltaPreNodeFactory.undoPropertyChanges(fs, propertyChanges);
		return fs;
	}

	public static ArchiveLibFileSetImpl createLibFileset() {
		return new ArchiveLibFileSetImpl();
	}

}
