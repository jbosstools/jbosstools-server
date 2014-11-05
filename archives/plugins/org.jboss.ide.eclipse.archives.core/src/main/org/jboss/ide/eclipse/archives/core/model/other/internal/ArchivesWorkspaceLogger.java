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

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.IArchivesLogger;

/**
 * @author eskimo
 *  Comment:
 *       This could probably be worked into ArchivesCore API
 *       This specific implementation should belong in an eclipse-specific
 *       class.
 */
public class ArchivesWorkspaceLogger implements IArchivesLogger {
	/**
	 * Copy of ArchiveCorePlugin.PLUGIN_ID to break dependency cycle between
	 * jars in this plug-in
	 *
	 * @see ArchiveCorePlugin.PLUGIN_ID
	 */
	public static final String PLUGIN_ID = ArchivesCore.PLUGIN_ID;

	static ILog log = Platform.getLog(Platform.getBundle(PLUGIN_ID));

	public void log(int severity, String message,Throwable t) {
		IStatus status = new Status(severity, PLUGIN_ID, message, t);
		log(status);
	}
	public void log(IStatus status) {
		log.log(status);
	}
}
