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
package org.jboss.tools.as.test.core.internal.utils;

import org.eclipse.wst.server.core.IModuleType;

public class MockModuleType implements IModuleType {
	private String id, name, version;
	public MockModuleType(String id, String name, String version) {
		this.id = id; this.name = name; this.version = version;
	}
	public String getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public String getVersion() {
		return version;
	}
}