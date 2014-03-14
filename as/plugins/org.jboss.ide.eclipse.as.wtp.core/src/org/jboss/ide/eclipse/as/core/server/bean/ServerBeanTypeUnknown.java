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

public class ServerBeanTypeUnknown extends JBossServerType {

	protected ServerBeanTypeUnknown() {
		super(
			UNKNOWN_STR, UNKNOWN_STR,
			"",//$NON-NLS-1$
			new String[]{V7_0, V7_1, V7_2, V6_0, V6_1, V6_2, V6_3, V5_1, V5_2, V5_3, V5_0, V4_3, V4_2, V4_0, V3_4, V3_3, V3_2}, 
			null);
	}

}
