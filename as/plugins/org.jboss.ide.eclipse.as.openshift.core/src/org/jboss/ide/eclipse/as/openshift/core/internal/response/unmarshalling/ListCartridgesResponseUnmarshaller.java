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

import java.util.ArrayList;
import java.util.List;

import org.jboss.dmr.ModelNode;
import org.jboss.ide.eclipse.as.openshift.core.Cartridge;
import org.jboss.ide.eclipse.as.openshift.core.ICartridge;
import org.jboss.ide.eclipse.as.openshift.core.internal.IOpenshiftJsonConstants;

/**
 * WARNING: the current (9-7-2011) response from the openshift rest service is
 * invalid. It quotes the nested json object in the data property: '"data" :
 * "{'. My current unmarshalling code does not handle this bad json.
 * 
 * @author André Dietisheim
 */
public class ListCartridgesResponseUnmarshaller extends AbstractOpenshiftJsonResponseUnmarshaller<List<ICartridge>> {

	@Override
	protected List<ICartridge> createOpenshiftObject(ModelNode responseNode) {
		List<ICartridge> cartridges = new ArrayList<ICartridge>();
		ModelNode dataNode = responseNode.get(IOpenshiftJsonConstants.PROPERTY_DATA);
		if (dataNode == null) {
			return cartridges;
		}
		ModelNode cartridgesNode = dataNode.get(IOpenshiftJsonConstants.PROPERTY_CARTS);
		if (cartridgesNode == null) {
			return cartridges;
		}
		for (ModelNode cartridgeNode : cartridgesNode.asList()) {
			cartridges.add(createCartridge(cartridgeNode));
		}
		return cartridges;
	}

	private Cartridge createCartridge(ModelNode cartridgeNode) {
		String name = cartridgeNode.asString();
		return new Cartridge(name);
	}
}
