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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
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

import org.jolokia.client.J4pClient;
import org.jolokia.client.exception.J4pException;
import org.jolokia.client.request.J4pExecRequest;
import org.jolokia.client.request.J4pExecResponse;
import org.jolokia.client.request.J4pListRequest;
import org.jolokia.client.request.J4pListResponse;
import org.jolokia.client.request.J4pReadRequest;
import org.jolokia.client.request.J4pResponse;
import org.jolokia.client.request.J4pSearchRequest;
import org.jolokia.client.request.J4pSearchResponse;
import org.jolokia.client.request.J4pWriteRequest;
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
		HashSet<String> result = new HashSet<String>();
		try {
			 Set<ObjectName> on = queryNames(new ObjectName("*:*"), null);
			 Iterator<ObjectName> it = on.iterator();
			 while(it.hasNext()) {
				 result.add(it.next().getDomain());
			 }
			 return (String[]) result.toArray(new String[result.size()]);
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
		J4pListRequest request;
		try {
			request = new J4pListRequest(name);
			J4pListResponse resp = j4pClient.execute(request, type);
			JSONObject o = resp.getValue();
			return new JolokiaMBeanUtility().createMBeanInfoFromSingletonList(o);
		} catch (J4pException e) {
			throw new IOException(e);
		}
	}
	
	@Override
	public boolean isRegistered(ObjectName name) throws IOException {
		return queryNames(name, null).size() > 0;
	}


	@Override
	public Set<ObjectName> queryNames(ObjectName name, QueryExp query) throws IOException {
		
		J4pSearchRequest request;
		try {
			request = new J4pSearchRequest(name.getCanonicalName());
			/*
			 * Due to UNDERTOW-879  GET doesn't work because undertow refuses 
			 * to escape certain characters, so must use POST
			 * 
			 * However, POST does not work behind CDK (oddly enough). 
			 * Behind CDK, GET works, but will still fail on the same unescaped characters
			 */
			J4pSearchResponse resp = j4pClient.execute(request, type);
			HashSet<ObjectName> toFilter = new HashSet<ObjectName>(resp.getObjectNames());
			
			// TODO filter using query
			
			return toFilter;
		} catch (MalformedObjectNameException e) {
			throw new IOException(e);
		} catch (J4pException e) {
			throw new IOException(e);
		}
	}

	
	
	
	
	
	/*  Get / set attributes */
	
	
	@Override
	public void setAttribute(ObjectName name, Attribute attribute)
			throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException,
			MBeanException, ReflectionException, IOException {
		J4pWriteRequest req = new J4pWriteRequest(name, attribute.getName(), attribute.getValue());
		try {
			List<J4pResponse<J4pWriteRequest>> c = j4pClient.execute(req);  // TODO type??? GET or POST,  API missing?
			Iterator<J4pResponse<J4pWriteRequest>> it = c.iterator();
			while(it.hasNext()) {
				J4pResponse<J4pWriteRequest> r = it.next();
				Object o = r.asJSONObject().get("status");
				if( o == null ) {
					// We don't know what happened
				} else if( !o.equals(new Long(200))) {
					throw new IOException("Failed to update attribute " + attribute.getName() + " on object " + name.getCanonicalName());
				}
			}
		} catch (J4pException e) {
			throw new IOException(e);
		}
	}

	@Override
	public AttributeList setAttributes(ObjectName name, AttributeList attributes)
			throws InstanceNotFoundException, ReflectionException, IOException {
		AttributeList result = new AttributeList();
		Iterator<Attribute> i = attributes.asList().iterator();
		while(i.hasNext()) {
			Attribute a = i.next();
			try {
				setAttribute(name, a);
				result.add(a);
			} catch (AttributeNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidAttributeValueException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MBeanException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
	}


	@Override
	public Object getAttribute(ObjectName name, String attribute) throws MBeanException, AttributeNotFoundException,
			InstanceNotFoundException, ReflectionException, IOException {
		AttributeList l = getAttributes(name, new String[]{attribute});
		if( l.size() > 0 ) {
			return l.get(0);
		}
		return null;
	}

	@Override
	public AttributeList getAttributes(ObjectName name, String[] attributes)
			throws InstanceNotFoundException, ReflectionException, IOException {
		AttributeList al = new AttributeList();
		J4pReadRequest req = null;
		req = new J4pReadRequest(name, attributes);
		List<J4pResponse<J4pReadRequest>> resp = null;
		try {
			resp = j4pClient.execute(req);  // TODO type??? GET or POST,  API missing?
		} catch (J4pException e) {
			throw new IOException(e);
		}
		if( resp != null ) {
			Iterator<J4pResponse<J4pReadRequest>> c = resp.iterator();
			while(c.hasNext()) {
				J4pResponse<J4pReadRequest> r2 = c.next();
				Object v = r2.getValue();
				al.add(v);
			}
		}
		return al;
	}

	
	/* Operation Invocations */
	@Override
	public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature)
			throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
		J4pExecRequest req = new J4pExecRequest(name, operationName, params);
		J4pExecResponse resp;
		try {
			resp = j4pClient.execute(req);
			Object response = resp.getValue();
			return response;
		} catch (J4pException e) {
			throw new IOException(e);
		}
	}


	
	
	/*
	 * The following methods are *CURRENTLY* unsupported due to missing classname in json response. 
	 */
	

	@Override
	public ObjectInstance getObjectInstance(ObjectName name) throws InstanceNotFoundException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isInstanceOf(ObjectName name, String className) throws InstanceNotFoundException, IOException {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public Set<ObjectInstance> queryMBeans(ObjectName name, QueryExp query) throws IOException {
		// TODO Auto-generated method stub
		return null;
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
			throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException,
			NotCompliantMBeanException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName)
			throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException,
			NotCompliantMBeanException, InstanceNotFoundException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObjectInstance createMBean(String className, ObjectName name, Object[] params, String[] signature)
			throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException,
			NotCompliantMBeanException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName, Object[] params,
			String[] signature) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException,
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
