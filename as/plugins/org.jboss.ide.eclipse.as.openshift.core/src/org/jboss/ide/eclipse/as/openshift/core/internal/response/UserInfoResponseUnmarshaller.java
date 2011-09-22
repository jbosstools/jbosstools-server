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
import java.util.Date;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;

import org.jboss.dmr.ModelNode;
import org.jboss.ide.eclipse.as.openshift.core.Application;
import org.jboss.ide.eclipse.as.openshift.core.Cartridge;
import org.jboss.ide.eclipse.as.openshift.core.Domain;
import org.jboss.ide.eclipse.as.openshift.core.IOpenshiftJsonConstants;
import org.jboss.ide.eclipse.as.openshift.core.IOpenshiftService;
import org.jboss.ide.eclipse.as.openshift.core.ISSHPublicKey;
import org.jboss.ide.eclipse.as.openshift.core.SSHPublicKey;
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
	protected UserInfo createOpenshiftObject(ModelNode node) throws DatatypeConfigurationException {
		ModelNode dataNode = node.get(IOpenshiftJsonConstants.PROPERTY_DATA);
		if (!isSet(dataNode)) {
			return null;
		}

		ModelNode userInfoNode = dataNode.get(IOpenshiftJsonConstants.PROPERTY_USER_INFO);
		if (!isSet(userInfoNode)) {
			return null;
		}

		ISSHPublicKey sshKey = createSSHKey(userInfoNode);
		User user = createUser(userInfoNode, sshKey, createDomain(userInfoNode));
		List<Application> applications = createApplications(dataNode.get(IOpenshiftJsonConstants.PROPERTY_APP_INFO), user);

		return new UserInfo(user, applications);
	}

	private ISSHPublicKey createSSHKey(ModelNode userInfoNode) {
		String sshPublicKey = getString(IOpenshiftJsonConstants.PROPERTY_SSH_KEY, userInfoNode);
		return new SSHPublicKey(sshPublicKey);
	}

	private List<Application> createApplications(ModelNode appInfoNode, User user) throws DatatypeConfigurationException {
		List<Application> applications = new ArrayList<Application>();
		if (!isSet(appInfoNode)) {
			return applications;
		}

		for (String name : appInfoNode.keys()) {
			applications.add(createApplication(name, appInfoNode.get(name), user));
		}
		return applications;
	}

	private Application createApplication(String name, ModelNode appNode, User user) throws DatatypeConfigurationException {
		String embedded = getString(IOpenshiftJsonConstants.PROPERTY_EMBEDDED, appNode);
		String uuid = getString(IOpenshiftJsonConstants.PROPERTY_UUID, appNode);
		Cartridge cartrdige = new Cartridge(getString(IOpenshiftJsonConstants.PROPERTY_FRAMEWORK, appNode));
		Date creationTime = getDate(IOpenshiftJsonConstants.PROPERTY_CREATION_TIME, appNode);
		return new Application(name, uuid, cartrdige, embedded, creationTime, user, service);
	}

	private User createUser(ModelNode userInfoNode, ISSHPublicKey sshKey, Domain domain) {
		String rhlogin = getString(IOpenshiftJsonConstants.PROPERTY_RHLOGIN, userInfoNode);
		String uuid = getString(IOpenshiftJsonConstants.PROPERTY_UUID, userInfoNode);
		return new User(rhlogin, uuid, sshKey, domain);
	}

	private Domain createDomain(ModelNode userInfoNode) {
		String namespace = getString(IOpenshiftJsonConstants.PROPERTY_NAMESPACE, userInfoNode);
		String rhcDomain = getString(IOpenshiftJsonConstants.PROPERTY_RHC_DOMAIN, userInfoNode);
		return new Domain(namespace, rhcDomain);
	}
}
