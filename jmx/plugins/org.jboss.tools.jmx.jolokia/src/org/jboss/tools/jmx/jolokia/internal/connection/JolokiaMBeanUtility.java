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
		ArrayList<MBeanAttributeInfo> collector = new ArrayList<MBeanAttributeInfo>();
		Set entries = obj.keySet();
		Iterator i = entries.iterator();
		while(i.hasNext()) {
			String attrName = (String)i.next();
			JSONObject v = (JSONObject)obj.get(attrName);
			boolean writable = (Boolean)v.get("rw");
			String type = (String)v.get("type");
			String desc = (String)v.get("desc");
			MBeanAttributeInfo info = new MBeanAttributeInfo(attrName, type, desc, writable, writable, false);
			collector.add(info);
		}
		
		collector.sort(new Comparator<MBeanAttributeInfo>() {
			@Override
			public int compare(MBeanAttributeInfo o1, MBeanAttributeInfo o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		
		
		return (MBeanAttributeInfo[]) collector.toArray(new MBeanAttributeInfo[collector.size()]);
	}
	public MBeanOperationInfo[] getOperationInfos(JSONObject obj) {
		ArrayList<MBeanOperationInfo> collector = new ArrayList<MBeanOperationInfo>();
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
			ArrayList<MBeanParameterInfo> params = new ArrayList<MBeanParameterInfo>();
			while(argsIt.hasNext()) {
				JSONObject argsKey = (JSONObject)argsIt.next();
				String argName = (String)argsKey.get("name");
				String argType = (String)argsKey.get("type");
				String argDesc = (String)argsKey.get("desc");
				MBeanParameterInfo paramInfo = new MBeanParameterInfo(argName, argType, argDesc);
				params.add(paramInfo);
			}
			MBeanParameterInfo[] paramsArr = (MBeanParameterInfo[]) params.toArray(new MBeanParameterInfo[params.size()]);
			MBeanOperationInfo opInfo = new MBeanOperationInfo(opName, desc, paramsArr, ret, impact);
			collector.add(opInfo);
		}
		collector.sort(new Comparator<MBeanOperationInfo>() {
			@Override
			public int compare(MBeanOperationInfo o1, MBeanOperationInfo o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		return (MBeanOperationInfo[]) collector.toArray(new MBeanOperationInfo[collector.size()]);
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
