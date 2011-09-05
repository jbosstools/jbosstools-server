package org.jboss.ide.eclipse.as.openshift.internal.test.core;

import org.jboss.ide.eclipse.as.openshift.core.Domain;
import org.jboss.ide.eclipse.as.openshift.core.DomainException;
import org.jboss.ide.eclipse.as.openshift.core.DomainFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DomainFactoryTest {

	@Before
	public void setUp() {
		
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test
	public void canCreateDomain() throws DomainException {
		DomainFactory domainFactory = new DomainFactory();
		Domain domain = domainFactory.create();
	}
	
}
