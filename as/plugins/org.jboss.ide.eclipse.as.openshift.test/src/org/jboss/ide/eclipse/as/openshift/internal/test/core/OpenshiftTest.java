package org.jboss.ide.eclipse.as.openshift.internal.test.core;

import static org.junit.Assert.assertEquals;

import org.jboss.ide.eclipse.as.openshift.core.Openshift;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;
import org.jboss.ide.eclipse.as.openshift.core.internal.marshalling.OpenshiftJsonRequestFactory;
import org.jboss.ide.eclipse.as.openshift.core.internal.marshalling.UserInfoRequestJsonMarshaller;
import org.jboss.ide.eclipse.as.openshift.internal.core.request.UserInfoRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OpenshiftTest {

	private Openshift openshift;
	
	private static final String USERNAME = "dietisheim@gmx.net";
	private static final String PASSWORD = "1q2w3e";

	@Before
	public void setUp() {
		this.openshift = new Openshift(USERNAME, PASSWORD);
	}

	@After
	public void tearDown() {

	}

	@Test
	public void canMarshallRequestCorrectly() throws OpenshiftException {
		String expectedRequestString = "password=" + PASSWORD + "&json_data=%7B%22rhlogin%22+%3A+%22dietisheim%40gmx.net%22%2C+%22debug%22+%3A+%22true%22%7D";
		
		String userInfoRequest = new UserInfoRequestJsonMarshaller(new UserInfoRequest(USERNAME, true)).create();
		String effectiveRequest = new OpenshiftJsonRequestFactory(PASSWORD, userInfoRequest).create();

		assertEquals(expectedRequestString, effectiveRequest);
	}
	
	@Test(expected=OpenshiftException.class)
	public void cannotGetUserInfoIfNotAppNorDomainCreated() throws OpenshiftException {
		openshift.getUserInfo();
	}

}
