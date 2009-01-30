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
package org.jboss.ide.eclipse.archives.core.model;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbPackages;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XMLBinding.XbException;


/**
 * An interface for methods relevent to a model's root node.
 * Only a label interface thus far
 * @author <a href="rob.stryker@redhat.com">Rob Stryker</a>
 *
 */
public interface IArchiveModelRootNode extends IArchiveNode {
	public static final double DESCRIPTOR_VERSION_1_0 = 1.0;
	public static final double DESCRIPTOR_VERSION_1_2 = 1.2;
	public static final double DESCRIPTOR_VERSION_LATEST = DESCRIPTOR_VERSION_1_2;
	
	public void setModel(IArchiveModel model);
	public IArchiveModel getModel();
	public void save(IProgressMonitor monitor) throws ArchivesModelException;
	public double getDescriptorVersion();
	public void setDescriptorVersion(double d);	
	public IPath getDescriptor();
}
