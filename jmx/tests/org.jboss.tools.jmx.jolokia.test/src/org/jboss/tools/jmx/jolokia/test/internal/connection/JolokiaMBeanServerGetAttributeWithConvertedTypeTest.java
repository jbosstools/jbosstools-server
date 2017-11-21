/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.jmx.jolokia.test.internal.connection;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.ObjectName;

import org.jboss.tools.jmx.jolokia.test.util.JolokiaTestEnvironmentSetup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class JolokiaMBeanServerGetAttributeWithConvertedTypeTest extends JolokiaTestEnvironmentSetup {

	private static final String JOLOKIA_IT_OBJECTNAME_OPERATION = JOLOKIA_IT_DOMAIN+":type=attributetest";
	
	private ObjectName objectWithAttributes;

	@Before
	public void setup() throws Exception {
		super.setup();
		objectWithAttributes = new ObjectName(JOLOKIA_IT_OBJECTNAME_OPERATION);
	}
	
	@Test
	public void testGetLongAttribute() throws Exception {
		Attribute firstAttributeToChange = new Attribute("ALongAttribute", 55L);
		jolokiaMBeanServerConnection.setAttribute(objectWithAttributes, firstAttributeToChange);
		
		assertThat(jolokiaMBeanServerConnection.getAttribute(objectWithAttributes, "ALongAttribute")).isEqualTo(55L);
	}
	
	@Test
	public void testGetIntAttribute() throws Exception {
		Attribute intAttributeToChange = new Attribute("AnIntAttribute", 5);
		jolokiaMBeanServerConnection.setAttribute(objectWithAttributes, intAttributeToChange);
		
		assertThat(jolokiaMBeanServerConnection.getAttribute(objectWithAttributes, "AnIntAttribute")).isEqualTo(5);
	}
	
	@Test
	public void testGetDateAttribute() throws Exception {
		Date date = new Date(75);
		Attribute dateAttributeToChange = new Attribute("ADateAttribute", date);
		jolokiaMBeanServerConnection.setAttribute(objectWithAttributes, dateAttributeToChange);
		
		assertThat((Date)jolokiaMBeanServerConnection.getAttribute(objectWithAttributes, "ADateAttribute")).isCloseTo(date, 500);
		//TODO : use isEqualTo instead of isCloseTo when https://github.com/rhuss/jolokia/issues/329 is fixed
	}
	
	@Test
	public void testGetSeveralAttributes() throws Exception {
		AttributeList list = new AttributeList();
		
		//TODO: add back Date when https://github.com/rhuss/jolokia/issues/329 is fixed
//		Attribute dateAttributeToChange = new Attribute("ADateAttribute", new Date());
//		list.add(dateAttributeToChange);
		Attribute intAttributeToChange = new Attribute("AnIntAttribute", 52);
		list.add(intAttributeToChange);
		Attribute longAttributeToChange = new Attribute("ALongAttribute", 56L);
		list.add(longAttributeToChange);
		
		jolokiaMBeanServerConnection.setAttributes(objectWithAttributes, list);
		
		assertThat(jolokiaMBeanServerConnection.getAttributes(objectWithAttributes, new String[]{/*"ADateAttribute",*/ "AnIntAttribute", "ALongAttribute"}))
		.containsOnly(/*dateAttributeToChange,*/ intAttributeToChange, longAttributeToChange);
	}
	
}
