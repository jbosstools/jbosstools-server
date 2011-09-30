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
package org.jboss.ide.eclipse.as.openshift.core;

import java.util.Date;

/**
 * @author André Dietisheim
 */
public interface IApplication {

	public String getName();
	
	public String getUUID() throws OpenshiftException;

	public ICartridge getCartridge();

	public String getEmbedded() throws OpenshiftException;

	public Date getCreationTime() throws OpenshiftException;

	public void destroy() throws OpenshiftException;

	public void start() throws OpenshiftException;

	public void restart() throws OpenshiftException;

	public void stop() throws OpenshiftException;

	public ApplicationLogReader getLogReader() throws OpenshiftException;

	public String getGitUri() throws OpenshiftException;

	public String getApplicationUrl() throws OpenshiftException;

}