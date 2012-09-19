/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server;

import java.util.List;
import java.util.Properties;

public interface INeedCredentials extends IServerProvider {

	/**
	 * Get a list of required properties for these credentials
	 * Ex:  username, password, security realm, etc
	 * @return
	 */
	public List<String> getRequiredProperties();
	
	/**
	 * Provides the required credentials to the INeedCredentials object
	 * @param credentials  A property map, mapping each String property to a String value
	 */
	public void provideCredentials(Properties credentials);

}
