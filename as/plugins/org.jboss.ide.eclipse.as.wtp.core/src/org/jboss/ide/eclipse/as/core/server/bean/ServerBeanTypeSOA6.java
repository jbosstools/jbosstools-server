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
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;

public class ServerBeanTypeSOA6 extends ServerBeanTypeUnknownAS71Product {
	public ServerBeanTypeSOA6() {
		super("SOA", "JBoss Enterprise SOA Platform", 
				asPath("modules","system","layers","base","org","jboss","as","server","main"), 
				new SOA6ServerTypeCondition());
	}
	public static class SOA6ServerTypeCondition extends UnknownAS72ProductServerTypeCondition {
		public String getServerTypeId(String version) {
			return IJBossToolingConstants.SERVER_EAP_61;
		}
		@Override
		public boolean isServerRoot(File location) {
			return "soa".equals(getSlot(location)) && super.isServerRoot(location);
		}
	}
}
