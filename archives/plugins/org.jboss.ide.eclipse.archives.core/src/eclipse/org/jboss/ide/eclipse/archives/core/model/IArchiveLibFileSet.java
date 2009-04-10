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

public interface IArchiveLibFileSet extends IArchiveFileSet {
	public static final String ATTRIBUTE_PREFIX = "org.jboss.ide.eclipse.archives.core.model.IArchiveLibFileSet."; //$NON-NLS-1$
	public static final String ID_ATTRIBUTE = ATTRIBUTE_PREFIX + "id"; //$NON-NLS-1$
	public void setId(String id);
	public String getId();
}
