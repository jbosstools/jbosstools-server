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
package org.jboss.ide.eclipse.as.core.client;

import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.JBossServer;

/**
 * This is merely here to label classes by the interface so that
 * the Publish client shows up in the list of possible clients.
 * 
 * The Publish client should take no action and let the previous
 * call to publish() do all the work.
 * 
 * @author rstryker
 *
 */
public interface ICopyAsLaunch {
	public boolean supportsDeploy(IServer server, String launchMode);
}
