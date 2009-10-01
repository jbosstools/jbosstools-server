/******************************************************************************* 
 * Copyright (c) 2009 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.wtp.core.modules;

import org.eclipse.wst.server.core.IModule;


public interface IJBTModule {
	/**
	 * Returns the modules contained within this module. The returned modules 
	 * can be either modulecore projects or representations of binary jars
	 * 
	 * @return a possibly empty array of modules contained within this application
	 */
	public IModule[] getModules();

	/**
	 * Returns the URI of the given module within this enterprise application.
	 * 
	 * @param module a module within this application
	 * @return the URI of the given module, or <code>null</code> if the URI could
	 *    not be found
	 */
	public String getURI(IModule module);

	/**
	 * Returns true if this is a binary (zipped) module, and
	 * false if it is expanded.
	 * 
	 * <p>If true, members() should return only a single element -
	 * the binary (jar or zip file) that contains the contents of
	 * this module. (a single IModuleResource, e.g.
	 * myejb.jar) Also, getLocation() should return the full path
	 * up to and including the binary itself. (e.g.
	 * c:\temp\myejb.jar)</p>
	 * 
	 * <p>If false, members() should return the entire contents
	 * of the module, starting at the root. There should be no
	 * preceeding directory structure. (an array of
	 * IModuleResources, e.g. index.html, WEB-INF/web.xml,
	 * ...) In this case, getLocation() should return the path to
	 * the root folder containing these resources.</p>
	 * 
	 * @return boolean
	 */
	public boolean isBinary();
}
