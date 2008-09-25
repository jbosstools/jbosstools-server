/*
 * JBoss, a division of Red Hat
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
package org.jboss.ide.eclipse.archives.core.model.internal.xb;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.ArchivesCoreMessages;


public class XbProperties extends XbPackageNode {

	private PropertiesExt properties;

	public XbProperties () {
		super();
		this.properties = new PropertiesExt();
	}

	public XbProperties (XbProperties props) {
		super(props);
		this.properties = new PropertiesExt();
		for (Iterator iter = getChildren(XbProperty.class).iterator(); iter.hasNext(); ) {
			XbProperty element = (XbProperty) iter.next();
			addProperty(element);
		}
	}

	protected Object clone() throws CloneNotSupportedException {
		return new XbProperties(this);
	}

	public class PropertiesExt extends Properties {
		private static final long serialVersionUID = 1L;
		private Hashtable propertyElements;

		public PropertiesExt () {
			propertyElements = new Hashtable();
		}

		/**
		 * Will map String key -> XbProperty element(key,value) in the local structure
		 * and map String key -> String value in the superclass
		 *
		 * @return The String value held previously
		 */
		public synchronized Object put(Object key, Object value) {
			if( key == null )
				throw new NullPointerException( ArchivesCore.bind(
						ArchivesCoreMessages.KeyIsNull, getClass().getName()));

			if( value == null )
				throw new NullPointerException( ArchivesCore.bind(
									ArchivesCoreMessages.ValueIsNull, getClass().getName()));

			if (!propertyElements.containsKey(key)) {
				XbProperty element = new XbProperty();
				element.setName((String)key);
				element.setValue((String)value);
				propertyElements.put(key, element);
			} else {
				XbProperty element = (XbProperty)propertyElements.get(key);
				element.setValue((String)value);
			}

			return super.put(key, value);
		}

		public synchronized Object remove(Object key) {
			propertyElements.remove(key);
			return super.remove(key);
		}

		public Collection getPropertyElements () {
			return propertyElements.values();
		}
	}

	public PropertiesExt getProperties () {
		return properties;
	}

	public void addProperty (Object property) {
		addProperty((XbProperty)property);
	}

	public void addProperty (XbProperty property) {
		properties.setProperty(property.getName(), property.getValue());
		addChild(property);
	}


}
