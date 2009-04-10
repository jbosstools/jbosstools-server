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

import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNodeDelta;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbPackageNode;

public interface INodeProvider {
	public boolean canCreateNode(XbPackageNode node);
	public IArchiveNode createNode(XbPackageNode node);
	public boolean canCreateDelta(IArchiveNode node);
	public IArchiveNode createDelta(IArchiveNodeDelta parentDelta, IArchiveNode postChange, 
				HashMap attributeChanges, HashMap propertyChanges);
}
