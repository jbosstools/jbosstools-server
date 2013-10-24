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
import java.util.ArrayList;

import org.jboss.ide.eclipse.as.core.server.bean.ServerBeanTypeUnknownAS72Product.UnknownAS72ProductServerTypeCondition;

/**
 * @since 2.4  Actually 2.4.101
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

		@Override
		protected String[] getManifestFoldersToFindVersion(String productSlot,
				String[] layers) {
			String[] folders = super.getManifestFoldersToFindVersion(productSlot, layers);
			String[] newFolders = new String[folders.length + 1];
			// https://issues.jboss.org/browse/JBIDE-15765
			String add = getMetaInfFolderForSlot("eap");
			System.arraycopy(folders, 0, newFolders, 0, folders.length);
			newFolders[folders.length] = add;
			return newFolders;
		}
	}
}
