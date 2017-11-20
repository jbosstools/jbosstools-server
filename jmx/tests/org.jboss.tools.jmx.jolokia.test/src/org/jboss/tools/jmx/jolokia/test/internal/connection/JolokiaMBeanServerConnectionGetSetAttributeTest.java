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
import static org.junit.Assume.assumeFalse;

import java.nio.CharBuffer;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.ObjectName;

import org.jboss.tools.jmx.jolokia.test.util.JolokiaTestEnvironmentSetup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class JolokiaMBeanServerConnectionGetSetAttributeTest extends JolokiaTestEnvironmentSetup {

	private static final String JOLOKIA_IT_OBJECTNAME_OPERATION = JOLOKIA_IT_DOMAIN+":type=attributetest";
	private static final String A_SECOND_ATTRIBUTE = "ASecondAttribute";
	private static final String AN_ATTRIBUTE = "AnAttribute";
	
	@Test
	public void testSetAttribute() throws Exception {
		ObjectName objectWithAttributes = new ObjectName(JOLOKIA_IT_OBJECTNAME_OPERATION);
		
		jolokiaMBeanServerConnection.setAttribute(objectWithAttributes, new Attribute(AN_ATTRIBUTE, "aValue"));
		
		assertThat(jolokiaMBeanServerConnection.getAttribute(objectWithAttributes, AN_ATTRIBUTE)).isEqualTo("aValue");
	}
	
	@Test
	public void testSetAttributeWithLotOfCharactersValue() throws Exception {
		assumeFalse("GET".equals(requestType));
		String longParameter = CharBuffer.allocate(10000+1).toString().replaceAll("\0", "a");
		ObjectName objectWithAttributes = new ObjectName(JOLOKIA_IT_OBJECTNAME_OPERATION);
		
		jolokiaMBeanServerConnection.setAttribute(objectWithAttributes, new Attribute(AN_ATTRIBUTE, longParameter));
		
		assertThat(jolokiaMBeanServerConnection.getAttribute(objectWithAttributes, AN_ATTRIBUTE)).isEqualTo(longParameter);
	}
	
	@Test
	public void testSetAttributes() throws Exception {
		AttributeList attrList = new AttributeList();
		Attribute firstAttributeToChange = new Attribute(AN_ATTRIBUTE, "aValueForSetAttributes");
		attrList.add(firstAttributeToChange);
		Attribute secondAttributeToChange = new Attribute(A_SECOND_ATTRIBUTE, "aSecondValue");
		attrList.add(secondAttributeToChange);
		ObjectName objectWithAttributes = new ObjectName(JOLOKIA_IT_OBJECTNAME_OPERATION);
		
		AttributeList settedAttributes = jolokiaMBeanServerConnection.setAttributes(objectWithAttributes, attrList);
		
		assertThat(settedAttributes).containsExactly(firstAttributeToChange, secondAttributeToChange);
		assertThat(jolokiaMBeanServerConnection.getAttribute(objectWithAttributes, AN_ATTRIBUTE)).isEqualTo(firstAttributeToChange.getValue());
		assertThat(jolokiaMBeanServerConnection.getAttribute(objectWithAttributes, A_SECOND_ATTRIBUTE)).isEqualTo(secondAttributeToChange.getValue());
	}
	
	@Test
	public void testSetAttributesIncludingAVeryLongOne() throws Exception {
		assumeFalse("GET".equals(requestType));
		String longParameter = CharBuffer.allocate(10000+1).toString().replaceAll("\0", "a");
		AttributeList attrList = new AttributeList();
		Attribute firstAttributeToChange = new Attribute(AN_ATTRIBUTE, "aValueForSetAttributes");
		attrList.add(firstAttributeToChange);
		Attribute secondAttributeToChange = new Attribute(A_SECOND_ATTRIBUTE, longParameter);
		attrList.add(secondAttributeToChange);
		ObjectName objectWithAttributes = new ObjectName(JOLOKIA_IT_OBJECTNAME_OPERATION);
		
		AttributeList settedAttributes = jolokiaMBeanServerConnection.setAttributes(objectWithAttributes, attrList);
		
		assertThat(settedAttributes).containsExactly(firstAttributeToChange, secondAttributeToChange);
		assertThat(jolokiaMBeanServerConnection.getAttribute(objectWithAttributes, AN_ATTRIBUTE)).isEqualTo(firstAttributeToChange.getValue());
		assertThat(jolokiaMBeanServerConnection.getAttribute(objectWithAttributes, A_SECOND_ATTRIBUTE)).isEqualTo(secondAttributeToChange.getValue());
	}
	
	@Test
	public void testGetAttributes() throws Exception {
		AttributeList attrList = new AttributeList();
		Attribute firstAttributeToChange = new Attribute(AN_ATTRIBUTE, "aValueForGetAttributes");
		attrList.add(firstAttributeToChange);
		Attribute secondAttributeToChange = new Attribute(A_SECOND_ATTRIBUTE, "aSecondValueForGetAttributes");
		attrList.add(secondAttributeToChange);
		ObjectName objectWithAttributes = new ObjectName(JOLOKIA_IT_OBJECTNAME_OPERATION);
		jolokiaMBeanServerConnection.setAttributes(objectWithAttributes, attrList);
		
		AttributeList attributes = jolokiaMBeanServerConnection.getAttributes(objectWithAttributes, new String[]{AN_ATTRIBUTE, A_SECOND_ATTRIBUTE});
		
		assertThat(attributes).containsExactly(firstAttributeToChange, secondAttributeToChange);
	}
	
}
