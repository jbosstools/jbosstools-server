/*
 * JBoss, a division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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
