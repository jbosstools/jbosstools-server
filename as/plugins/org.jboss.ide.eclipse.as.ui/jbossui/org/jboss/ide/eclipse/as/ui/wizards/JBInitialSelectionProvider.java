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
package org.jboss.ide.eclipse.as.ui.wizards;

import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.ui.internal.viewers.InitialSelectionProvider;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class JBInitialSelectionProvider extends InitialSelectionProvider {

	public JBInitialSelectionProvider() {
	}
	
	public IServerType getInitialSelection(IServerType[] serverTypes) {
		if (serverTypes == null)
			return null;
		
		int size = serverTypes.length;
		for (int i = 0; i < size; i++) {
			if( serverTypes[i].getId().equals("org.jboss.ide.eclipse.as.42")) //$NON-NLS-1$
				return serverTypes[i];
		}
		return serverTypes[0];
	}


}
