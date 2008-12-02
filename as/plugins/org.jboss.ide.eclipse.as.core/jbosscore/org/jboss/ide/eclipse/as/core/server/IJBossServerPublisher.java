/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ide.eclipse.as.core.server;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;

/**
 * 
 * @author Rob Stryker
 *
 */
public interface IJBossServerPublisher {
	public static final int NO_PUBLISH = 0;
	public static final int INCREMENTAL_PUBLISH = 1;
	public static final int FULL_PUBLISH = 2;
	public static final int REMOVE_PUBLISH = 3;
	

	// pre-publish
	public boolean accepts(IServer server, IModule[] module);
	
	
	public IStatus publishModule(IServer server, IModule[] module, 
			int publishType, IModuleResourceDelta[] delta, IProgressMonitor monitor) throws CoreException;
	
	// post publish
	public int getPublishState();
}
