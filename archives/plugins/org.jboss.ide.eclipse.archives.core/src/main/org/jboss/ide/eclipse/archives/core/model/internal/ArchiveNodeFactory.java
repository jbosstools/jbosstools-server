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
package org.jboss.ide.eclipse.archives.core.model.internal;

import java.util.HashMap;

import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveAction;
import org.jboss.ide.eclipse.archives.core.model.IArchiveStandardFileSet;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFolder;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNodeDelta;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNodeFactory;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbAction;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbFileSet;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbFolder;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbPackage;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbPackageNode;

/**
 * Just a factory for extenders to access our secret internals
 * @author <a href="rob.stryker@redhat.com">Rob Stryker</a>
 *
 */
public class ArchiveNodeFactory implements IArchiveNodeFactory {
	public IArchive createArchive() {
		return new ArchiveImpl();
	}
	
	public IArchiveStandardFileSet createFileset() {
		return new ArchiveFileSetImpl();
	}
	
	public IArchiveFolder createFolder() {
		return new ArchiveFolderImpl();
	}
	
	public IArchiveAction createAction() {
		return new ArchiveActionImpl();
	}

	public IArchiveNode createNode(XbPackageNode node) {
		ArchiveNodeImpl nodeImpl = null;
		if (node instanceof XbPackage) {
			nodeImpl = new ArchiveImpl((XbPackage)node);
		} else if (node instanceof XbFolder) {
			nodeImpl = new ArchiveFolderImpl((XbFolder)node);
		} else if (node instanceof XbFileSet) {
			nodeImpl = new ArchiveFileSetImpl((XbFileSet)node);
		} else if( node instanceof XbAction ) {
			nodeImpl = new ArchiveActionImpl((XbAction)node);
		}
		return nodeImpl;
	}
	
	public IArchiveNode createDeltaNode(IArchiveNodeDelta parentDelta, IArchiveNode postChange, 
			HashMap attributeChanges, HashMap propertyChanges) {
		return new ArchiveDeltaPreNodeFactory().createNode(parentDelta, postChange, attributeChanges, propertyChanges);
	}
}
