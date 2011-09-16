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

import java.util.ArrayList;
import java.util.List;

import org.jboss.dmr.ModelNode;
import org.jboss.ide.eclipse.as.openshift.core.Application;
import org.jboss.ide.eclipse.as.openshift.core.Cartridge;
import org.jboss.ide.eclipse.as.openshift.core.Domain;
import org.jboss.ide.eclipse.as.openshift.core.IOpenshiftJsonConstants;
import org.jboss.ide.eclipse.as.openshift.core.IOpenshiftService;
import org.jboss.ide.eclipse.as.openshift.core.User;
import org.jboss.ide.eclipse.as.openshift.core.UserInfo;

/**
 * @author André Dietisheim
 */
public class UserInfoResponseUnmarshaller extends AbstractOpenshiftJsonResponseUnmarshaller<UserInfo> {

	private IOpenshiftService service;

	public UserInfoResponseUnmarshaller(IOpenshiftService service) {
		this.service = service;
	}

	@Override
	protected UserInfo createOpenshiftObject(ModelNode node) {
		ModelNode dataNode = node.get(IOpenshiftJsonConstants.PROPERTY_DATA);
		if (dataNode == null) {
			return null;
		}
		ModelNode userInfoNode = node.get(IOpenshiftJsonConstants.PROPERTY_USER_INFO);
		if (userInfoNode == null) {
			return null;
		}
		Domain domain = createDomain(userInfoNode);
		User user = createUser(userInfoNode, domain);
		return new UserInfo(
				user,
				createApplications(node.get(IOpenshiftJsonConstants.PROPERTY_APP_INFO)));
	}

	private List<Application> createApplications(ModelNode appInfoNode) {
		List<Application> applications = new ArrayList<Application>();
		if (appInfoNode == null) {
			return applications;
		}

		for(String name : appInfoNode.keys()) {
			applications.add(createApplication(name, appInfoNode.get(name)));
		}
		return applications;
	}


	private Application createApplication(String name, ModelNode appNode) {
		String embedded = getString(IOpenshiftJsonConstants.PROPERTY_EMBEDDED, appNode);
		String uuid = getString(IOpenshiftJsonConstants.PROPERTY_UUID, appNode);
		Cartridge cartrdige = new Cartridge(getString(IOpenshiftJsonConstants.PROPERTY_FRAMEWORK, appNode));
		long creationTime = getLong(IOpenshiftJsonConstants.PROPERTY_FRAMEWORK, appNode);
		return new Application(name, uuid, cartrdige, embedded, creationTime, service);
	}

	protected User createUser(ModelNode userInfoNode, Domain domain) {
		String rhlogin = getString(IOpenshiftJsonConstants.PROPERTY_RHLOGIN, userInfoNode);
		String uuid = getString(IOpenshiftJsonConstants.PROPERTY_RHLOGIN, userInfoNode);
		return new User(rhlogin, uuid, domain);
	}

	protected Domain createDomain(ModelNode userInfoNode) {
		String namespace = getString(IOpenshiftJsonConstants.PROPERTY_NAMESPACE, userInfoNode);
		String rhcDomain = getString(IOpenshiftJsonConstants.PROPERTY_RHC_DOMAIN, userInfoNode);
		return new Domain(namespace, rhcDomain);
	}

}
