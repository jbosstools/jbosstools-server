/******************************************************************************* 
 * Copyright (c) 2013 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server;


/**
 * An interface representing any class that can provide a JMX url
 * through which it can be accessed.  
 * 
 * @since 3.0 (actually 2.4.101)
 */
public interface IJMXURLProvider {
	
	/**
	 * Get a JMX url that is capable of handling connections to this object. 
	 * JBoss servers in the 7.x stream, for example, may return something like 
	 * service:jmx:remoting-jmx://localhost:9999
	 * 
	 * @return
	 */
	public String getJMXUrl();
}
