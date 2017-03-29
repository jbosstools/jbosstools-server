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
import java.util.Comparator;
import java.util.Iterator;
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
	}
	
	public MBeanOperationInfo[] getOperationInfos(JSONObject obj) {
		ArrayList<MBeanOperationInfo> collector = new ArrayList<>();
		Set entries = obj.keySet();
		Iterator i = entries.iterator();
		while(i.hasNext()) {
			String opName = (String)i.next();
			JSONObject v = (JSONObject)obj.get(opName);
			String ret = (String)v.get("ret");
			String desc = (String)v.get("desc");
			int impact = MBeanOperationInfo.UNKNOWN;
			JSONArray args = (JSONArray)v.get("args");
			Iterator argsIt = args.iterator();
			ArrayList<MBeanParameterInfo> params = new ArrayList<>();
			while(argsIt.hasNext()) {
				JSONObject argsKey = (JSONObject)argsIt.next();
				String argName = (String)argsKey.get("name");
				String argType = (String)argsKey.get("type");
				String argDesc = (String)argsKey.get("desc");
				MBeanParameterInfo paramInfo = new MBeanParameterInfo(argName, argType, argDesc);
				params.add(paramInfo);
			}
			MBeanParameterInfo[] paramsArr = params.toArray(new MBeanParameterInfo[params.size()]);
			MBeanOperationInfo opInfo = new MBeanOperationInfo(opName, desc, paramsArr, ret, impact);
			collector.add(opInfo);
		}
		collector.sort(Comparator.comparing(MBeanOperationInfo::getName));
		return collector.toArray(new MBeanOperationInfo[collector.size()]);
	}
	
	
	/* Currently unavailable */
	public MBeanConstructorInfo[] getContructorInfos(JSONObject obj) {
		return new MBeanConstructorInfo[]{};
	}
	public MBeanNotificationInfo[] getNotificationInfos(JSONObject obj) {
		return new MBeanNotificationInfo[]{};
	}
	
	
	public MBeanInfo createMBeanInfoFromSingletonList(JSONObject obj) {
		String className = "Unknown";
		Object description = obj.get("description");
		String desc = description == null ? "null" : description.toString();
        MBeanAttributeInfo[] attributes = getAttributeInfos( (JSONObject)obj.get("attr"));
        MBeanOperationInfo[] operations = getOperationInfos( (JSONObject)obj.get("op"));
        MBeanConstructorInfo[] constructors = getContructorInfos(null);
        MBeanNotificationInfo[] notifications = getNotificationInfos(null);
        return new MBeanInfo(className, desc, attributes, constructors, operations, notifications);
	}
	

	

}
