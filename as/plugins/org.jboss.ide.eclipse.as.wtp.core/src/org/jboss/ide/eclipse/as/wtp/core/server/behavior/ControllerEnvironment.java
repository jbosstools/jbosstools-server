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
package org.jboss.ide.eclipse.as.wtp.core.server.behavior;

import java.util.HashMap;
import java.util.Map;

/**
 * A class for building an environment to be passed to a queried controller
 * A convenience class for quickly building environment maps
 */
public class ControllerEnvironment {
	private HashMap<String, Object> map = new HashMap<String, Object>();
	public ControllerEnvironment addProperty(String key, Object val) {
		map.put(key, val);
		return this;
	}
	
	/**
	 * Add a required property that the dependency must have set
	 * 
	 * @param system
	 * @param key
	 * @param val
	 * @return
	 */
	public ControllerEnvironment addRequiredProperty(String system, String key, String val) {
		// Ignore requests on a null key
		if( key != null )
			return addRequiredProperties(system, new String[]{key}, new String[]{val});
		return this;
	}
	
	/**
	 * Add a required property that the dependency subsystem must have set
	 * 
	 * @param system
	 * @param key
	 * @param val
	 * @return
	 */
	public ControllerEnvironment addRequiredProperties(String system, String[] key, String[] val) {
		if( key == null || val == null )
			return this;
		StringBuilder builder = new StringBuilder();
		for( int i = 0; i < key.length;i++ ) {
			if( key[i] != null ) {
				builder.append(key[i]);
				builder.append("=");
				builder.append(val[i] == null ? "" : val[i]);
				builder.append(";");
			}
		}
		String propKey = system + AbstractSubsystemController.REQUIRED_PROPERTIES_ENV_KEY;
		map.put(propKey, builder.toString());
		return this;
	}
	
	public Map<String, Object> getMap() {
		return map;
	}
}
