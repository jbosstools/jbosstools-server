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

public interface IPreferenceManager {
	public boolean isBuilderEnabled(IPath path);
	public void setBuilderEnabled(IPath path, boolean val);
	public boolean areProjectSpecificPrefsEnabled(IPath path);
	public void setProjectSpecificPrefsEnabled(IPath path, boolean val);
}
