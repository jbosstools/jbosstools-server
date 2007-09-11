/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.ui.views.server.providers.events;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.jboss.ide.eclipse.as.core.extensions.events.EventLogModel.EventLogTreeItem;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.IEventLogLabelProvider;

/**
 *
 * @author rob.stryker@jboss.com
 */
public abstract class ComplexEventLogLabelProvider 
		extends LabelProvider implements IEventLogLabelProvider {
	protected static String DELIMITER = "::";
	
	protected ArrayList supported;
	protected HashMap propertyToMessageMap;
	public ComplexEventLogLabelProvider() {
		supported = new ArrayList();
		propertyToMessageMap = new HashMap();
		addSupportedTypes();
		loadPropertyMap();
	}
	protected abstract void addSupportedTypes();
	protected abstract void loadPropertyMap();

	public boolean supports(String type) {
		supported.clear(); addSupportedTypes();
		return supported.contains(type);
	}

	public Properties getProperties(EventLogTreeItem item) {
		loadPropertyMap();
		Properties p = new Properties();
		HashMap map = item.getProperties();
		Object key = null;
		String keyString, valueStringKey, valueString;
		for( Iterator i = map.keySet().iterator(); i.hasNext();) {
			try {
			key = i.next();
			if( key.equals(EventLogTreeItem.DATE)) {
				keyString = propertyToMessageMap.get(key) == null ? (String)key : propertyToMessageMap.get(key).toString();
				valueString = getDateAsString(((Long)map.get(key)).longValue());
				p.put(keyString, valueString);
			} else {
				keyString = propertyToMessageMap.get(key) == null ? (String)key : propertyToMessageMap.get(key).toString();
				valueStringKey = key + DELIMITER + toString2(map.get(key));
				valueString = propertyToMessageMap.get(valueStringKey) == null ? toString2(map.get(key)) : toString2(propertyToMessageMap.get(valueStringKey));
				p.put(keyString, valueString);
			}
			} catch( Exception e ) { e.printStackTrace(); }
		}
		return p;
	}
	
	protected String getDateAsString(long date) {
		long now = new Date().getTime();
		long seconds = (now - date) / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		minutes -= (hours * 60);
		String minString = minutes + "m ago";
		if( hours == 0 )
			return minString;
		return hours + "h " + minString; 
	}

	private String toString2(Object o) {
		return o == null ? "null" : o.toString();
	}

}
