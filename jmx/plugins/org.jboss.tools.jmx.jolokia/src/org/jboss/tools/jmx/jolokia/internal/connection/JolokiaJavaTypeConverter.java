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
package org.jboss.tools.jmx.jolokia.internal.connection;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.openmbean.SimpleType;

import org.jboss.tools.jmx.jolokia.internal.Activator;
import org.jolokia.converter.Converters;
import org.jolokia.converter.json.JsonConvertOptions;
import org.jolokia.converter.object.StringToOpenTypeConverter;
import org.jolokia.util.DateUtil;
import org.json.simple.JSONArray;

public class JolokiaJavaTypeConverter {
	
	private Converters jolokiaConverters = new Converters();
	
	public Object getJson(Object value){
		try {
			return jolokiaConverters.getToJsonConverter().convertToJson(value, null, JsonConvertOptions.DEFAULT);
		} catch (AttributeNotFoundException e) {
			Activator.pluginLog().logError(e);
		}
		return value;
	}
	
	public Object getConvertedToCorrectTypeReturnedValue(MBeanAttributeInfo mBeanAttributeInfo, Object jolokiaReturnedValue) {
		String realType = mBeanAttributeInfo != null ? mBeanAttributeInfo.getType() : null;
		return getConvertedToCorrectType(jolokiaReturnedValue, realType);
	}
	
	public Object getConvertedToCorrectType(Object jolokiaReturnedValue, String realType) {
		if(realType != null && jolokiaReturnedValue instanceof String){
			return getConvertedString(jolokiaReturnedValue, realType);
		} else if(realType != null && jolokiaReturnedValue instanceof Number){
			return getConvertedNumeric((Number)jolokiaReturnedValue, realType);
		} else if(realType != null && jolokiaReturnedValue instanceof JSONArray){
			return getConvertedCollection((JSONArray) jolokiaReturnedValue, realType);
		}
		return jolokiaReturnedValue;
	}

	protected Object getConvertedString(Object jolokiaReturnedValue, String realType) {
		if(Date.class.getName().equals(realType)) {
			return DateUtil.fromISO8601((String) jolokiaReturnedValue);
		} else {
			return jolokiaConverters.getToObjectConverter().convertFromString(realType, (String)jolokiaReturnedValue);
		}
	}

	protected Object getConvertedCollection(JSONArray jolokiaReturnedValue, String realType) {
		if(Set.class.getName().equals(realType) || HashSet.class.getName().equals(realType)) {
			return new HashSet<Object>(jolokiaReturnedValue);
		} else {
			return jolokiaReturnedValue;
		}
	}

	protected Object getConvertedNumeric(Number jolokiaReturnedValue, String realType) {
		StringToOpenTypeConverter openTypeConverter = jolokiaConverters.getToOpenTypeConverter();
		if("int".equals(realType) || Integer.class.getName().equals(realType)) {
			return openTypeConverter.convertToObject(SimpleType.INTEGER, jolokiaReturnedValue);
		} else if("short".equals(realType) || Short.class.getName().equals(realType)) {
			return openTypeConverter.convertToObject(SimpleType.SHORT, jolokiaReturnedValue);
		} else if("float".equals(realType) || Float.class.getName().equals(realType)) {
			return openTypeConverter.convertToObject(SimpleType.FLOAT, jolokiaReturnedValue);
		}
		return jolokiaReturnedValue;
	}
	
}
