/******************************************************************************* 
 * Copyright (c) 2016 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.bean;

public class ServerBeanTypeEnterprise extends JBossServerType {

	protected ServerBeanTypeEnterprise(String id, String name, String jbossSystemJarPath, String[] versions,
			ICondition condition) {
		super(id, name, jbossSystemJarPath, versions, condition);
	}
	protected String getServerTypeBaseName() {
		return "Red Hat JBoss " + getId();
	}
}
