/**
 * JBoss, a Division of Red Hat
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
package org.jboss.ide.eclipse.as.core.packages;

import org.eclipse.wst.server.core.IModule;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.IArchiveType;
import org.jboss.ide.eclipse.as.core.packages.types.EarArchiveType;
import org.jboss.ide.eclipse.as.core.packages.types.EjbArchiveType;
import org.jboss.ide.eclipse.as.core.packages.types.WarArchiveType;

/**
 *
 * @author rob.stryker@jboss.com
 */
public class ModulePackageTypeConverter {
	public static IArchiveType getPackageTypeFor(IModule module) {
		String modType = module.getModuleType().getId();
		if("jst.web".equals(modType)) {
			return ArchivesCore.getInstance().getExtensionManager().getArchiveType(WarArchiveType.WAR_PACKAGE_TYPE);
		} else if("jst.ear".equals(modType)) {
			return ArchivesCore.getInstance().getExtensionManager().getArchiveType(EarArchiveType.ID);
		} else if("jst.ejb".equals(modType)) {
			return ArchivesCore.getInstance().getExtensionManager().getArchiveType(EjbArchiveType.ID);
		}

		return null;
	}
}
