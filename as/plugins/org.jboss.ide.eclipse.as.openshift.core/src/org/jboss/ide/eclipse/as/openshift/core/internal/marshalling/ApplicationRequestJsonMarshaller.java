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
package org.jboss.ide.eclipse.as.openshift.core.internal.marshalling;

import org.jboss.dmr.ModelNode;
import org.jboss.ide.eclipse.as.openshift.core.Cartridge;
import org.jboss.ide.eclipse.as.openshift.core.IOpenshiftJsonConstants;
import org.jboss.ide.eclipse.as.openshift.internal.core.request.ApplicationAction;
import org.jboss.ide.eclipse.as.openshift.internal.core.request.ApplicationRequest;

/**
 * @author André Dietisheim
 */
public class ApplicationRequestJsonMarshaller extends AbstractJsonMarshaller<ApplicationRequest> {

	@Override
	protected void setJsonDataProperties(ModelNode node, ApplicationRequest request) {
		node.get(IOpenshiftJsonConstants.PROPERTY_CARTRIDGE).set(getCartridgeName(request.getCartridge()));
		node.get(IOpenshiftJsonConstants.PROPERTY_ACTION).set(getActionName(request.getAction()));
		node.get(IOpenshiftJsonConstants.PROPERTY_APP_NAME).set(request.getName());
	}

	private String getCartridgeName(Cartridge cartridge) {
		if (cartridge == null) {
			return null;
		}
		return cartridge.getName();
	}

	private String getActionName(ApplicationAction action) {
		if (action == null) {
			return null;
		}
		return action.name().toLowerCase();
	}
}
