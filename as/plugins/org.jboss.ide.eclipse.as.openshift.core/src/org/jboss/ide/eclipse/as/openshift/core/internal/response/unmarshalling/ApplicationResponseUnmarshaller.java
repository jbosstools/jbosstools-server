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
package org.jboss.ide.eclipse.as.openshift.core.internal.response.unmarshalling;

import org.jboss.dmr.ModelNode;
import org.jboss.ide.eclipse.as.openshift.core.ICartridge;
import org.jboss.ide.eclipse.as.openshift.core.internal.Application;
import org.jboss.ide.eclipse.as.openshift.core.internal.OpenshiftService;
import org.jboss.ide.eclipse.as.openshift.core.internal.User;

/**
 * @author André Dietisheim
 */
public class ApplicationResponseUnmarshaller extends AbstractOpenshiftJsonResponseUnmarshaller<Application> {

	private User user;
	private String applicationName;
	private ICartridge cartridge;
	private OpenshiftService service;

	public ApplicationResponseUnmarshaller(String applicationName, ICartridge cartridge, User user, OpenshiftService service) {
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
