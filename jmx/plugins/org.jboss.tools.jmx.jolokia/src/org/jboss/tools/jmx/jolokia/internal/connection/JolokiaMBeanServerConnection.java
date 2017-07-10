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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;

import org.jboss.tools.jmx.jolokia.internal.Activator;
import org.jolokia.client.J4pClient;
import org.jolokia.client.exception.J4pException;
import org.jolokia.client.request.J4pExecRequest;
import org.jolokia.client.request.J4pExecResponse;
import org.jolokia.client.request.J4pListRequest;
import org.jolokia.client.request.J4pListResponse;
import org.jolokia.client.request.J4pQueryParameter;
import org.jolokia.client.request.J4pReadRequest;
import org.jolokia.client.request.J4pReadResponse;
import org.jolokia.client.request.J4pResponse;
import org.jolokia.client.request.J4pSearchRequest;
import org.jolokia.client.request.J4pSearchResponse;
import org.jolokia.client.request.J4pWriteRequest;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * A very basic implementation of MBeanServerConnection, using the jolokia client jar
 * to make the various requests to the server.  It is not 100% functional, but works for 
 * the base case that our UI uses. 
 * 
 * This code could be improved. 
 */
public class JolokiaMBeanServerConnection implements MBeanServerConnection {
	private J4pClient j4pClient;
	private String type; // GET or POST
	private JolokiaJavaTypeConverter converter = new JolokiaJavaTypeConverter();
	
	public JolokiaMBeanServerConnection(J4pClient j4pClient, String type) {
		this.j4pClient = j4pClient;
		this.type = type;
	}

	@Override
	public String getDefaultDomain() throws IOException {
		// don't know how to find this
		throw new IOException("Unsupported");
	}

	@Override
	public String[] getDomains() throws IOException {
		try {
			 Set<ObjectName> on = queryNames(new ObjectName("*:*"), null);
			 return on.stream()
					 .map(ObjectName::getDomain)
					 .toArray(String[]::new);
		} catch (MalformedObjectNameException e) {
			throw new IOException(e); // Should never happen
		}
	}

	@Override
	public Integer getMBeanCount() throws IOException {
		try {
			return queryNames(new ObjectName("*:*"), null).size();
		} catch (MalformedObjectNameException e) {
			throw new IOException(e); // Should never happen
		}
	}

	@Override
	public MBeanInfo getMBeanInfo(ObjectName name)
			throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException {
		try {
			J4pListRequest request = new J4pListRequest(name);
			J4pListResponse resp = j4pClient.execute(request, type);
			JSONObject o = resp.getValue();
			return new JolokiaMBeanUtility().createMBeanInfoFromSingletonList(o);
		} catch (J4pException e) {
			throw new IOException(e);
		}
	}
	
	@Override
	public boolean isRegistered(ObjectName name) throws IOException {
		return !queryNames(name, null).isEmpty();
	}


	@Override
	public Set<ObjectName> queryNames(ObjectName name, QueryExp query) throws IOException {
		try {
			J4pSearchRequest request = new J4pSearchRequest(name.getCanonicalName());
			Map<J4pQueryParameter,String> processingOptions = new EnumMap<>(J4pQueryParameter.class);
			processingOptions.put(J4pQueryParameter.CANONICAL_NAMING, Boolean.FALSE.toString());
			/*
			 * Due to UNDERTOW-879  GET doesn't work because undertow refuses 
			 * to escape certain characters, so must use POST
			 * 
			 * However, POST does not work behind CDK (oddly enough). 
			 * Behind CDK, GET works, but will still fail on the same unescaped characters
			 */
			J4pSearchResponse resp = j4pClient.execute(request, type, processingOptions);
			HashSet<ObjectName> toFilter = new HashSet<>(resp.getObjectNames());
			
			return toFilter;
		} catch (MalformedObjectNameException | J4pException e) {
			throw new IOException(e);
		}
	}
	
	/*  Get / set attributes */
	
	
	@Override
	public void setAttribute(ObjectName name, Attribute attribute)
			throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException,
			MBeanException, ReflectionException, IOException {
		J4pWriteRequest req = new J4pWriteRequest(name, attribute.getName(), converter.getJson(attribute.getValue()));
		try {
			J4pResponse<J4pWriteRequest> r = j4pClient.execute(req);  // TODO type??? GET or POST,  API missing?
			Object o = r.asJSONObject().get("status");
			if( o == null ) {
				// We don't know what happened
			} else if( !o.equals(Long.valueOf(200))) {
				throw new IOException("Failed to update attribute " + attribute.getName() + " on object " + name.getCanonicalName());
			}
		} catch (J4pException e) {
			throw new IOException(e);
		}
	}

	@Override
	public AttributeList setAttributes(ObjectName name, AttributeList attributes)
			throws InstanceNotFoundException, ReflectionException, IOException {
		AttributeList result = new AttributeList();
		for (Attribute attribute : attributes.asList()) {
			try {
				setAttribute(name, attribute);
				result.add(attribute);
			} catch (AttributeNotFoundException | InvalidAttributeValueException | MBeanException e) {
				Activator.pluginLog().logError(e);
			}
		}
		return result;
	}

	@Override
	public Object getAttribute(ObjectName name, String attribute) throws MBeanException, AttributeNotFoundException,
			InstanceNotFoundException, ReflectionException, IOException {
		AttributeList l = getAttributes(name, new String[]{attribute});
		if( !l.isEmpty() ) {
			return l.get(0);
		}
		return null;
	}

	@Override
	public AttributeList getAttributes(ObjectName name, String[] attributeNames)
			throws InstanceNotFoundException, ReflectionException, IOException {
		List<MBeanAttributeInfo> attributesInfos = getAttributesInfos(name);
		
		
		AttributeList al = new AttributeList();
		J4pReadRequest req = new J4pReadRequest(name, attributeNames);
		Object response = null;
		try {
			response = j4pClient.execute(req); // TODO type??? GET or POST,  API missing?
		} catch (J4pException e) {
			throw new IOException(e);
		}
		if(response instanceof List) {
			List<J4pResponse<J4pReadRequest>> resp = (List<J4pResponse<J4pReadRequest>>)response;
			Iterator<J4pResponse<J4pReadRequest>> c = resp.iterator();
			while(c.hasNext()) {
				al.addAll(extractAttributesFromResponse(attributesInfos, c.next()));
			}
		} else if(response instanceof J4pReadResponse){
			if(attributeNames.length == 1){
				MBeanAttributeInfo mBeanAttributeInfo = findAttributeInfoWithName(attributesInfos, attributeNames[0]);
				al.add(converter.getConvertedToCorrectTypeReturnedValue(mBeanAttributeInfo, ((J4pReadResponse) response).getValue()));
			} else {
				al.addAll(extractAttributesFromResponse(attributesInfos, (J4pReadResponse) response));
			}
		}
		return al;
	}

	private List<MBeanAttributeInfo> getAttributesInfos(ObjectName name)
			throws InstanceNotFoundException, ReflectionException, IOException {
		try {
			MBeanInfo mBeanInfo = getMBeanInfo(name);
			return Arrays.asList(mBeanInfo.getAttributes());
		} catch (IntrospectionException e) {
			Activator.pluginLog().logError(e);
		}
		return Collections.emptyList();
	}

	private List<Object> extractAttributesFromResponse(List<MBeanAttributeInfo> attributesInfos, J4pResponse<?> response) {
		List<Object> extractedAttributes = new ArrayList<>();
		Object o22 = response.getValue();
		if(o22 instanceof JSONObject){
			Set<Map.Entry<String, Object>> entrySet = ((JSONObject)o22).entrySet();
			for (Map.Entry<String, Object> entry : entrySet) {
				String attributeName = entry.getKey();
				MBeanAttributeInfo mBeanAttributeInfo = findAttributeInfoWithName(attributesInfos, attributeName);
				Object jolokiaReturnedValue = entry.getValue();
				Object convertedToCorrectTypeReturnedValue = converter.getConvertedToCorrectTypeReturnedValue(mBeanAttributeInfo, jolokiaReturnedValue);
				extractedAttributes.add(new Attribute(attributeName, convertedToCorrectTypeReturnedValue));
			}
		} else {
			// A single value is returned for simple attribute
			extractedAttributes.add(o22);
		}
		return extractedAttributes;
	}


	private MBeanAttributeInfo findAttributeInfoWithName(List<MBeanAttributeInfo> attributesInfos, String attributeName) {
		return attributesInfos.parallelStream()
				.filter(attributeInfo -> attributeName.equals(attributeInfo.getName()))
				.findAny().orElse(null);
	}
	
	/* Operation Invocations */
	@Override
	public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature)
			throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
		String operationNameWithSignature = createOperationNameWithSignature(operationName, signature);
		String specifiedReturnedType = getSpecifiedReturnedType(name, operationName, params);
		
		J4pExecRequest req = createJ4pExecRequest(name, params, operationNameWithSignature);
		try {
			J4pExecResponse resp = j4pClient.execute(req);
			return converter.getConvertedToCorrectType(resp.getValue(), specifiedReturnedType);
		} catch (J4pException e) {
			throw new IOException(e);
		}
	}

	private String getSpecifiedReturnedType(ObjectName name, String operationName, Object[] params) throws InstanceNotFoundException, ReflectionException, IOException {
		try {
			MBeanInfo mBeanInfo = getMBeanInfo(name);
			if(mBeanInfo != null && params != null){
				List<MBeanOperationInfo> operations = Arrays.asList(mBeanInfo.getOperations()).stream()
						.filter(operationInfo -> operationName.equals(operationInfo.getName()))
						.filter(operationInfo -> operationInfo.getSignature() != null && params.length == operationInfo.getSignature().length)
						.collect(Collectors.toList());

				if(operations.size() == 1){
					return operations.get(0).getReturnType();
				} else {
					Activator.pluginLog().logInfo("Method invocation of "+ operationName +" might return the wrong Return Type due to current implementation limitations.");
				}
			}
					
		} catch (IntrospectionException e1) {
			Activator.pluginLog().logError(e1);
		}
		return null;
	}

	private J4pExecRequest createJ4pExecRequest(ObjectName name, Object[] params, String operationNameWithSignature) {
		if(params == null || params.length == 0){
			return new J4pExecRequest(name, operationNameWithSignature);
		} else {
			return new J4pExecRequest(name, operationNameWithSignature, params);
		}
	}

	private String createOperationNameWithSignature(String operationName, String[] signature) {
		StringJoiner stringJoiner = new StringJoiner(",", "(", ")");
		Stream.of(signature).forEach(stringJoiner::add);
		return operationName + stringJoiner.toString();
	}

	@Override
	public ObjectInstance getObjectInstance(ObjectName name) throws InstanceNotFoundException, IOException {
		return createObjectInstance(name);
	}

	@Override
	public Set<ObjectInstance> queryMBeans(ObjectName name, QueryExp query) throws IOException {
		Set<ObjectInstance> res = new HashSet<>();
		try {
			J4pSearchRequest req = new J4pSearchRequest(name.getCanonicalName());
			J4pResponse<J4pSearchRequest> j4pResponse = j4pClient.execute(req);
			Object value = j4pResponse.getValue();
			if(value instanceof JSONArray){
				for (Object mbean : (JSONArray)value) {
					if(mbean instanceof String){
						res.add(createObjectInstance((String)mbean));
					}
				}
			}
		} catch (MalformedObjectNameException | J4pException e) {
			Activator.pluginLog().logError(e);
		}
		return res;
	}

	private ObjectInstance createObjectInstance(String mbean) throws MalformedObjectNameException {
		ObjectName objectName = new ObjectName(mbean);
		return createObjectInstance(objectName);
	}

	private ObjectInstance createObjectInstance(ObjectName objectName) {
		String classname = retrieveClassName(objectName);
		return new ObjectInstance(objectName, classname);
	}

	private String retrieveClassName(ObjectName objectName) {
		String escapedCanonicalPropertyList = objectName.getCanonicalKeyPropertyListString().replaceAll("/", "!/");
		J4pListRequest listAttributes = new J4pListRequest(objectName.getDomain()+"/"+escapedCanonicalPropertyList+"/class");
		try {
			J4pResponse<J4pListRequest> listAttributesResponse = j4pClient.execute(listAttributes);
			return listAttributesResponse.getValue();
		} catch (J4pException e) {
			Activator.pluginLog().logError(e);
		}
		return "";
	}
	
	
	@Override
	public boolean isInstanceOf(ObjectName name, String className) throws InstanceNotFoundException, IOException {
		String mBeanClass = retrieveClassName(name);
		if(className != null && className.equals(mBeanClass)){
			return true;
		}
		try {
			return Class.forName(mBeanClass).isInstance(Class.forName(className));
		} catch (ClassNotFoundException e) {
			return false;
		}
	}	
	
	/*
	 * Unsupported operations are below.  
	 * At this time I have no intention on implementing these operations, 
	 * though contributions are welcome. 
	 */

	
	
	/* Add / Remove mbeans */

	@Override
	public void unregisterMBean(ObjectName name)
			throws InstanceNotFoundException, MBeanRegistrationException, IOException {
		// TODO Auto-generated method stub
		
	}

	
	@Override
	public ObjectInstance createMBean(String className, ObjectName name)
			throws ReflectionException, InstanceAlreadyExistsException, MBeanException,
			NotCompliantMBeanException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName)
			throws ReflectionException, InstanceAlreadyExistsException, MBeanException,
			NotCompliantMBeanException, InstanceNotFoundException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObjectInstance createMBean(String className, ObjectName name, Object[] params, String[] signature)
			throws ReflectionException, InstanceAlreadyExistsException, MBeanException,
			NotCompliantMBeanException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName, Object[] params,
			String[] signature) throws ReflectionException, InstanceAlreadyExistsException,
			MBeanException, NotCompliantMBeanException, InstanceNotFoundException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	
	
	
	/* Notifications */

	@Override
	public void addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter,
			Object handback) throws InstanceNotFoundException, IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter,
			Object handback) throws InstanceNotFoundException, IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeNotificationListener(ObjectName name, ObjectName listener)
			throws InstanceNotFoundException, ListenerNotFoundException, IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeNotificationListener(ObjectName name, NotificationListener listener)
			throws InstanceNotFoundException, ListenerNotFoundException, IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter,
			Object handback) throws InstanceNotFoundException, ListenerNotFoundException, IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter,
			Object handback) throws InstanceNotFoundException, ListenerNotFoundException, IOException {
		// TODO Auto-generated method stub
		
	}

	
}
