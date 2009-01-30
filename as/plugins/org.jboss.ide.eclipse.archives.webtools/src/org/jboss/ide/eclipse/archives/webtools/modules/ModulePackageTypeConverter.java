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
package org.jboss.ide.eclipse.archives.webtools.modules;

import org.eclipse.wst.server.core.IModule;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.IArchiveType;
import org.jboss.ide.eclipse.archives.webtools.archivetypes.EarArchiveType;
import org.jboss.ide.eclipse.archives.webtools.archivetypes.EjbArchiveType;
import org.jboss.ide.eclipse.archives.webtools.archivetypes.WarArchiveType;

/**
 *
 * @author rob.stryker@jboss.com
 */
public class ModulePackageTypeConverter {
	public static IArchiveType getPackageTypeFor(IModule module) {
		String modType = module.getModuleType().getId();
		if("jst.web".equals(modType)) {//$NON-NLS-1$
			return ArchivesCore.getInstance().getExtensionManager().getArchiveType(WarArchiveType.WAR_PACKAGE_TYPE);
		} else if("jst.ear".equals(modType)) {//$NON-NLS-1$
			return ArchivesCore.getInstance().getExtensionManager().getArchiveType(EarArchiveType.ID);
		} else if("jst.ejb".equals(modType)) {//$NON-NLS-1$
			return ArchivesCore.getInstance().getExtensionManager().getArchiveType(EjbArchiveType.ID);
		}

		return null;
	}
}
