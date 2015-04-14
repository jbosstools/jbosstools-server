/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.bean;

import java.io.File;

public interface ICondition {
	/**
	 * Is this location the root of an installation?
	 * @param location
	 * @return
	 */
	public boolean isServerRoot(File location);
	
	/**
	 * Get the full version of this server. Provide the system jar / reference file 
	 * as a hint. 
	 * 
	 * @param serverRoot
	 * @param systemFile
	 * @return
	 */
	public String getFullVersion(File serverRoot, File systemFile);
	
	/**
	 * Get the wtp server type which matches this ServerType with this installation
	 * 
	 * @param serverRoot
	 * @param systemFile
	 * @return
	 */
	public String getServerTypeId(String version);

}