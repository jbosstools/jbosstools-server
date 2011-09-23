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
package org.jboss.ide.eclipse.as.openshift.core.internal.response;

import org.jboss.dmr.ModelNode;
import org.jboss.ide.eclipse.as.openshift.core.Application;
import org.jboss.ide.eclipse.as.openshift.core.Cartridge;
import org.jboss.ide.eclipse.as.openshift.core.IOpenshiftService;
import org.jboss.ide.eclipse.as.openshift.core.User;

/**
 * @author André Dietisheim
 */
public class ApplicationResponseUnmarshaller extends AbstractOpenshiftJsonResponseUnmarshaller<Application> {

	private User user;
	private String applicationName;
	private Cartridge cartridge;
	private IOpenshiftService service;

	public ApplicationResponseUnmarshaller(String applicationName, Cartridge cartridge, User user, IOpenshiftService service) {
		this.applicationName = applicationName;
		this.cartridge = cartridge;
		this.user = user;
		this.service = service;
	}

	@Override
	protected Application createOpenshiftObject(ModelNode node) {
		return new Application(applicationName, cartridge, user, service);
	}
}
