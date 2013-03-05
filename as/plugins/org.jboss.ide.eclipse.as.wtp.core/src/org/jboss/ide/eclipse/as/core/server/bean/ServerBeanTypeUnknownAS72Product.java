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

import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;

public class ServerBeanTypeUnknownAS72Product extends ServerBeanTypeUnknownAS71Product {
	public ServerBeanTypeUnknownAS72Product() {
		super( asPath("modules","system","layers","base",
						"org","jboss","as","server","main"),
				new UnknownAS72ProductServerTypeCondition());
	}
	public static class UnknownAS72ProductServerTypeCondition extends UnknownAS71ProductServerTypeCondition {
		public String getServerTypeId(String version) {
			return IJBossToolingConstants.SERVER_EAP_61;
		}
		protected String getMetaInfFolderForSlot(String slot) {
			return "modules/system/layers/base/org/jboss/as/product/" + slot + "/dir/META-INF"; //$NON-NLS-1$
		}
	}
}
