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