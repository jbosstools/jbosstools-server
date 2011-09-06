package org.jboss.ide.eclipse.as.openshift.internal.test.core;

import static org.junit.Assert.assertEquals;

import java.net.URLEncoder;

import org.jboss.ide.eclipse.as.openshift.core.Openshift;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;
import org.jboss.ide.eclipse.as.openshift.core.internal.marshalling.ListCartridgesRequestJsonMarshaller;
import org.jboss.ide.eclipse.as.openshift.core.internal.marshalling.OpenshiftJsonRequestFactory;
import org.jboss.ide.eclipse.as.openshift.core.internal.marshalling.UserInfoRequestJsonMarshaller;
import org.jboss.ide.eclipse.as.openshift.internal.core.request.ListCartridgesRequest;
import org.jboss.ide.eclipse.as.openshift.internal.core.request.UserInfoRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OpenshiftIntegrationTest {

	private Openshift openshift;
	
	private static final String USERNAME = "toolsjboss@gmail.com";
	private static final String PASSWORD = "1q3e5t7u";

	@Before
	public void setUp() {
		this.openshift = new Openshift(USERNAME, PASSWORD);
	}

	@Test(expected=OpenshiftException.class)
	public void cannotGetUserInfoIfNotAppNorDomainCreated() throws OpenshiftException {
		openshift.getUserInfo();
	}

	@Test
	public void canRequestListCartridges() throws Exception {
		openshift.getCartridges();		
	}

	
}
