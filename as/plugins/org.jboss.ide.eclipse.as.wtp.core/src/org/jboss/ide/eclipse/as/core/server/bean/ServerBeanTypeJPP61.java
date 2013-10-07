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
package org.jboss.ide.eclipse.as.core.server.bean;

import java.io.File;

import org.jboss.ide.eclipse.as.core.server.bean.ServerBeanTypeUnknownAS72Product.UnknownAS72ProductServerTypeCondition;

/**
 * @since 3.0  Actually 2.4.101
 */
public class ServerBeanTypeJPP61 extends ServerBeanTypeUnknownAS71Product {
	public ServerBeanTypeJPP61() {
		super("JPP", "JBoss Portal Platform", 
				asPath("modules","system","layers","base","org","jboss","as","server","main"),
				new String[] { V6_1 },
				new JPP61Condition());
	}
	public static class JPP61Condition extends UnknownAS72ProductServerTypeCondition {
		@Override
		public boolean isServerRoot(File location) {
			if( "JPP".equalsIgnoreCase(getSlot(location))) {
				String v = getFullVersion(location, null);
				return v != null && v.startsWith(V6_1);
			}
			return false;
		}
	}
}
