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

import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanAttributeInfo;

import org.jboss.tools.jmx.jolokia.internal.connection.JolokiaMBeanUtility;
import org.json.simple.JSONObject;
import org.junit.Test;

public class JolokiaMBeanUtilityTest {

	@Test
	public void testGetAttributeInfoWithEmptyValue() throws Exception {
		MBeanAttributeInfo[] attributeInfos = new JolokiaMBeanUtility().getAttributeInfos(new JSONObject());
		
		assertThat(attributeInfos).isEmpty();
	}
	
	@Test
	public void testGetAttributeInfoForASingleValue() throws Exception {
		JSONObject obj = new JSONObject();
		Map<String, Object> map = new HashMap<>();
		map.put("type", "aType");
		map.put("desc", "aDescription");
		map.put("rw", true);
		JSONObject firstJSonObject = new JSONObject(map);
		obj.put("firstObject", firstJSonObject);
		
		MBeanAttributeInfo[] attributeInfos = new JolokiaMBeanUtility().getAttributeInfos(obj);
		
		assertThat(attributeInfos).containsExactly(new MBeanAttributeInfo("firstObject", "aType", "aDescription", true, true, false));
	}
	
	@Test
	public void testGetAttributeInfoForSeveralValues() throws Exception {
		JSONObject obj = new JSONObject();
		String objectKey = "firstObject";
		String typeValue = "aType";
		String descValue = "aDescription";
		boolean readWriteValue = true;
		JSONObject firstJSonObject = createJSOnObject(typeValue, descValue, readWriteValue);
		obj.put(objectKey, firstJSonObject);
		
		String objectKey2 = "asecondObjectInFirstPosition";
		String typeValue2 = "aSecondType";
		String descValue2 = "aSecondDescription";
		boolean readWriteValue2 = false;
		JSONObject secondJSonObject = createJSOnObject(typeValue2, descValue2, readWriteValue2);
		obj.put(objectKey2, secondJSonObject);
		
		MBeanAttributeInfo[] attributeInfos = new JolokiaMBeanUtility().getAttributeInfos(obj);
		
		assertThat(attributeInfos).containsExactly(
				new MBeanAttributeInfo(objectKey2, typeValue2, descValue2, readWriteValue2, readWriteValue2, false),
				new MBeanAttributeInfo(objectKey, typeValue, descValue, readWriteValue, readWriteValue, false));
	}

	private JSONObject createJSOnObject(String typeValue, String descValue, boolean readWriteValue) {
		Map<String, Object> map = new HashMap<>();
		map.put("type", typeValue);
		map.put("desc", descValue);
		map.put("rw", readWriteValue);
		JSONObject firstJSonObject = new JSONObject(map);
		return firstJSonObject;
	}
	
}
