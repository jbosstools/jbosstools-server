package org.jboss.tools.jmx.core.test;

import java.io.IOException;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServerConnection;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;

public class MockMBeanConnection implements MBeanServerConnection {
	
	public MBeanInfo getMBeanInfo(ObjectName name)
			throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException {
		// TODO Auto-generated method stub
		return new MBeanInfo("dummy", "dummy", new MBeanAttributeInfo[0], 
				new MBeanConstructorInfo[0], new MBeanOperationInfo[0], new MBeanNotificationInfo[0]);
	}


	public void setAttribute(ObjectName name, Attribute attribute)
			throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException,
			MBeanException, ReflectionException, IOException {
		// TODO Auto-generated method stub

	}

	public AttributeList setAttributes(ObjectName name, AttributeList attributes)
			throws InstanceNotFoundException, ReflectionException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getAttribute(ObjectName name, String attribute) throws MBeanException, AttributeNotFoundException,
			InstanceNotFoundException, ReflectionException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public AttributeList getAttributes(ObjectName name, String[] attributes)
			throws InstanceNotFoundException, ReflectionException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDefaultDomain() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getDomains() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public Integer getMBeanCount() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public ObjectInstance getObjectInstance(ObjectName name) throws InstanceNotFoundException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature)
			throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isInstanceOf(ObjectName name, String className) throws InstanceNotFoundException, IOException {
		// TODO Auto-generated method stub
		return false;
	}


	public boolean isRegistered(ObjectName name) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	public Set<ObjectInstance> queryMBeans(ObjectName name, QueryExp query) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<ObjectName> queryNames(ObjectName name, QueryExp query) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	
	
	
	
	
	public void removeNotificationListener(ObjectName name, ObjectName listener)
			throws InstanceNotFoundException, ListenerNotFoundException, IOException {
		// TODO Auto-generated method stub

	}

	public void removeNotificationListener(ObjectName name, NotificationListener listener)
			throws InstanceNotFoundException, ListenerNotFoundException, IOException {
		// TODO Auto-generated method stub

	}

	public void removeNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter,
			Object handback) throws InstanceNotFoundException, ListenerNotFoundException, IOException {
		// TODO Auto-generated method stub

	}

	public void removeNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter,
			Object handback) throws InstanceNotFoundException, ListenerNotFoundException, IOException {
		// TODO Auto-generated method stub

	}

	public void addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter,
			Object handback) throws InstanceNotFoundException, IOException {
		// TODO Auto-generated method stub

	}

	public void addNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter,
			Object handback) throws InstanceNotFoundException, IOException {
		// TODO Auto-generated method stub

	}

	public ObjectInstance createMBean(String className, ObjectName name)
			throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException,
			NotCompliantMBeanException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName)
			throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException,
			NotCompliantMBeanException, InstanceNotFoundException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public ObjectInstance createMBean(String className, ObjectName name, Object[] params, String[] signature)
			throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException,
			NotCompliantMBeanException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName, Object[] params,
			String[] signature) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException,
			MBeanException, NotCompliantMBeanException, InstanceNotFoundException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public void unregisterMBean(ObjectName name)
			throws InstanceNotFoundException, MBeanRegistrationException, IOException {
		// TODO Auto-generated method stub

	}

}
