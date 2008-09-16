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
package org.jboss.ide.eclipse.archives.core.model.types;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.jboss.ide.eclipse.archives.core.model.IActionType;
import org.jboss.ide.eclipse.archives.core.model.IArchiveAction;

/**
 * @author rob.stryker <rob.stryker@redhat.com>
 *
 */
public class AntActionType implements IActionType, IExecutableExtension {

	private IConfigurationElement element;
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		if( element == null ) element = config;
	}
	public String getId() {
		return element.getAttribute("id");
	}

	public String getLabel() {
		return element.getAttribute("label");
	}

	public void execute(IArchiveAction action) {
		System.out.println("Ant Working!");
	}
	
	public String getStringRepresentation(IArchiveAction action) {
		return "Ant action";
	}
}
