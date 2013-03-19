/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
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
