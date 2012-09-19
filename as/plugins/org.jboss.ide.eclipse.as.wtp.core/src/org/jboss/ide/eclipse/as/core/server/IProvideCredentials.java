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

public interface IProvideCredentials {
	/**
	 * Acknowledges whether the provider can or cannot provide credentials for this server
	 * 
	 * @param serverProvider an object to provide a server
	 * @param requiredProperties a list of required properties
	 * 
	 * @return true if this provider can handle this server; false otherwise
	 */
	public boolean accepts(IServerProvider serverProvider, List<String> requiredProperties);
	
	/**
	 * Handles the fetching / requesting and subsequent delivery of these credentials
	 * 
	 * @param inNeed the object requiring access to the credentials
	 * @param requiredProperties a list of required properties
	 * 
	 */
	public void handle(INeedCredentials inNeed, List<String> requiredProperties);
}
