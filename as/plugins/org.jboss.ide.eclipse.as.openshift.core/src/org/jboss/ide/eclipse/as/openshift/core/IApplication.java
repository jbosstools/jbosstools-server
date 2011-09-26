/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
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

	public abstract String getUUID() throws OpenshiftException;

	public abstract ICartridge getCartridge();

	public abstract String getEmbedded() throws OpenshiftException;

	public abstract Date getCreationTime() throws OpenshiftException;

	public abstract void destroy() throws OpenshiftException;

	public abstract void start() throws OpenshiftException;

	public abstract void restart() throws OpenshiftException;

	public abstract void stop() throws OpenshiftException;

	public abstract ApplicationLogReader getLog() throws OpenshiftException;

	public abstract String getGitUri() throws OpenshiftException;

	public abstract String getApplicationUrl() throws OpenshiftException;

}