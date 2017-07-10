/*******************************************************************************
 * Copyright (c) 2016 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.jmx.jolokia.internal.connection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JolokiaMBeanUtility {
	
	public MBeanAttributeInfo[] getAttributeInfos(JSONObject obj) {
		if(obj != null){
			return ((Set<Map.Entry<String, JSONObject>>)obj.entrySet()).stream()
					.map(entry ->
					{
						JSONObject value = entry.getValue();
						return new MBeanAttributeInfo(entry.getKey(),
								(String)value.get("type"),
								(String)value.get("desc"),
								(boolean)value.get("rw"),
								(boolean)value.get("rw"),
								false);
					})
					.sorted(Comparator.comparing(MBeanAttributeInfo::getName))
					.toArray(MBeanAttributeInfo[]::new);
		} else {
			return new MBeanAttributeInfo[]{};
		}
	}
	
	public MBeanOperationInfo[] getOperationInfos(JSONObject operations) {
		if(operations != null){
			ArrayList<MBeanOperationInfo> collector = new ArrayList<>();
			Set entries = operations.keySet();
			Iterator i = entries.iterator();
			while(i.hasNext()) {
				String opName = (String)i.next();
				collector.addAll(computeOperationInfos(operations, opName));
			}
			collector.sort(Comparator.comparing(MBeanOperationInfo::getName));
			return collector.toArray(new MBeanOperationInfo[collector.size()]);
		} else {
			return new MBeanOperationInfo[]{};
		}
	}

	private Set<MBeanOperationInfo> computeOperationInfos(JSONObject obj, String opName) {
		Object operationInfos = obj.get(opName);
		if(operationInfos instanceof JSONObject){
			return Collections.singleton(createOperationInfos(opName, (JSONObject)operationInfos));
		} else {
			//overloaded operations
			Set<MBeanOperationInfo> res = new HashSet<>();
			for(Object operationInfo : (JSONArray)operationInfos){
				res.add(createOperationInfos(opName, (JSONObject) operationInfo));
			}
			return res;
		}
	}

	private MBeanOperationInfo createOperationInfos(String opName, JSONObject operationInfo) {
		String ret = (String)operationInfo.get("ret");
		String desc = (String)operationInfo.get("desc");
		int impact = MBeanOperationInfo.UNKNOWN;
		MBeanParameterInfo[] paramsArr = computeParamInfos(operationInfo);
		return new MBeanOperationInfo(opName, desc, paramsArr, ret, impact);
	}

	private MBeanParameterInfo[] computeParamInfos(JSONObject v) {
		JSONArray args = (JSONArray)v.get("args");
		Iterator argsIt = args.iterator();
		List<MBeanParameterInfo> params = new ArrayList<>();
		while(argsIt.hasNext()) {
			JSONObject argsKey = (JSONObject)argsIt.next();
			String argName = (String)argsKey.get("name");
			String argType = (String)argsKey.get("type");
			String argDesc = (String)argsKey.get("desc");
			MBeanParameterInfo paramInfo = new MBeanParameterInfo(argName, argType, argDesc);
			params.add(paramInfo);
		}
		return params.toArray(new MBeanParameterInfo[params.size()]);
	}
	
	
	/* Currently unavailable */
	public MBeanConstructorInfo[] getContructorInfos(JSONObject obj) {
		return new MBeanConstructorInfo[]{};
	}
	public MBeanNotificationInfo[] getNotificationInfos(JSONObject obj) {
		return new MBeanNotificationInfo[]{};
	}
	
	
	public MBeanInfo createMBeanInfoFromSingletonList(JSONObject obj) {
		Object className = obj.get("class");
		String classNameForUser = className != null ? className.toString() : "Unknown";
		Object description = obj.get("description");
		String desc = description == null ? "null" : description.toString();
        MBeanAttributeInfo[] attributes = getAttributeInfos( (JSONObject)obj.get("attr"));
        MBeanOperationInfo[] operations = getOperationInfos( (JSONObject)obj.get("op"));
        MBeanConstructorInfo[] constructors = getContructorInfos(null);
        MBeanNotificationInfo[] notifications = getNotificationInfos(null);
        return new MBeanInfo(classNameForUser, desc, attributes, constructors, operations, notifications);
	}
	

	

}
