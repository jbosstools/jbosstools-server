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

import java.util.ArrayList;

import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;

public class ServerBeanTypeUnknownAS72Product extends ServerBeanTypeUnknownAS71Product {
	public ServerBeanTypeUnknownAS72Product() {
		super("EAP-Product", "EAP-Based Product", 
				asPath("modules","system","layers","base","org","jboss","as","server","main"), 
				new UnknownAS72ProductServerTypeCondition());
	}
	public static class UnknownAS72ProductServerTypeCondition extends UnknownAS71ProductServerTypeCondition {
		@Override
		public String getServerTypeId(String version) {
			return IJBossToolingConstants.SERVER_EAP_61;
		}
		@Override
		protected String[] getManifestFoldersToFindVersion(String productSlot, String[] layers) {
			ArrayList<String> l = new ArrayList<String>(layers.length);
			for( int i = 0; i < layers.length; i++ ) {
				l.add(getMetaInfFolderForLayer(layers[i]));
			}
			l.add(getMetaInfFolderForSlot(productSlot));
			return (String[]) l.toArray(new String[l.size()]);
		}
		@Override
		protected String getMetaInfFolderForSlot(String slot) {
			return "modules/system/layers/base/org/jboss/as/product/" + slot + "/dir/META-INF"; //$NON-NLS-1$
		}
		protected String getMetaInfFolderForLayer(String layer) {
			return "modules/system/layers/" + layer + "/org/jboss/as/product/" + layer + "/dir/META-INF"; //$NON-NLS-1$
		}
		
	}
}
