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
import java.util.Date;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;

import org.jboss.dmr.ModelNode;
import org.jboss.ide.eclipse.as.openshift.core.Cartridge;
import org.jboss.ide.eclipse.as.openshift.core.ICartridge;
import org.jboss.ide.eclipse.as.openshift.core.internal.ApplicationInfo;
import org.jboss.ide.eclipse.as.openshift.core.internal.IOpenshiftJsonConstants;
import org.jboss.ide.eclipse.as.openshift.core.internal.UserInfo;

/**
 * @author André Dietisheim
 */
public class UserInfoResponseUnmarshaller extends AbstractOpenshiftJsonResponseUnmarshaller<UserInfo> {

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

		String sshPublicKey = getString(IOpenshiftJsonConstants.PROPERTY_SSH_KEY, userInfoNode);
		String rhlogin = getString(IOpenshiftJsonConstants.PROPERTY_RHLOGIN, userInfoNode);
		String uuid = getString(IOpenshiftJsonConstants.PROPERTY_UUID, userInfoNode);
		String namespace = getString(IOpenshiftJsonConstants.PROPERTY_NAMESPACE, userInfoNode);
		String rhcDomain = getString(IOpenshiftJsonConstants.PROPERTY_RHC_DOMAIN, userInfoNode);

		List<ApplicationInfo> applicationInfos = createApplicationInfos(dataNode.get(IOpenshiftJsonConstants.PROPERTY_APP_INFO));

		return new UserInfo(rhlogin, uuid, sshPublicKey, rhcDomain, namespace, applicationInfos);
	}

	private List<ApplicationInfo> createApplicationInfos(ModelNode appInfoNode) throws DatatypeConfigurationException {
		List<ApplicationInfo> applicationInfos = new ArrayList<ApplicationInfo>();
		if (!isSet(appInfoNode)) {
			return applicationInfos;
		}

		for (String name : appInfoNode.keys()) {
			applicationInfos.add(createApplicationInfo(name, appInfoNode.get(name)));
		}
		return applicationInfos;
	}

	private ApplicationInfo createApplicationInfo(String name, ModelNode appNode) throws DatatypeConfigurationException {
		String uuid = getString(IOpenshiftJsonConstants.PROPERTY_UUID, appNode);
		String embedded = getString(IOpenshiftJsonConstants.PROPERTY_EMBEDDED, appNode);
		ICartridge cartrdige = new Cartridge(getString(IOpenshiftJsonConstants.PROPERTY_FRAMEWORK, appNode));
		Date creationTime = getDate(IOpenshiftJsonConstants.PROPERTY_CREATION_TIME, appNode);
		return new ApplicationInfo(name, uuid, embedded, cartrdige, creationTime);
	}
}
