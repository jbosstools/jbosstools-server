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
package org.jboss.ide.eclipse.as.core.module;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.internal.ModuleFactory;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;

public class ArchiveModuleFactory extends PathModuleFactory {
	
	private static String GENERIC_JAR = "jboss.archive";
	private static String VERSION = "1.0";
	
	public static final String FACTORY_ID = "org.jboss.ide.eclipse.as.core.ArchiveFactory";

	private static ArchiveModuleFactory factory;
	public static ArchiveModuleFactory getDefault() {
		if( factory == null ) {
			factory = (ArchiveModuleFactory)PathModuleFactory.getDefaultInstance(FACTORY_ID);
		}
		return factory;
	}
	
	public ArchiveModuleFactory() {
		super();
	}

	public boolean supports(String path) {
		try {
			File f = new File(path);
			JarFile jf = new JarFile(f);
			return true;
		} catch( IOException e ) {
		}
		return false;
	}

	public String getModuleType(String path) {
		return GENERIC_JAR;
	}

	public String getModuleVersion(String path) {
		return VERSION;
	}
}
