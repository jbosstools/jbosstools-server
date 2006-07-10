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
package org.jboss.ide.eclipse.as.core.server.runtime;

import java.util.ArrayList;

public class JBoss40RuntimeDelegate extends AbstractServerRuntimeDelegate {
	public static final String VERSION_ID = "org.jboss.ide.eclipse.as.runtime.40";
	
	public JBoss40RuntimeDelegate(JBossServerRuntime runtime) {
		super(runtime);
	}
	
	public String getId() {
		return "4.0";
	}

	public String[] getMinimalRequiredPaths() {
		ArrayList list = new ArrayList();
		list.add("conf");
		list.add("conf\\jboss-service.xml");
		list.add("conf\\jndi.properties");
		list.add("deploy");
		list.add("lib");
		list.add("lib\\jboss-management.jar");
		list.add("lib\\jboss-minimal.jar");
		list.add("lib\\jnpserver.jar");
		list.add("lib\\log4j.jar");
		list.add("log");
		String[] ret = new String[list.size()];
		list.toArray(ret);
		return ret;
	}

}
