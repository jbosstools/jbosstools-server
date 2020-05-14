/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.classpath.core.runtime.cache.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.tools.foundation.core.xml.XMLMemento;

public class ModuleAliasUtil {
	
	public ModuleSlot getAliasModuleSlot(File moduleXml) {
		if( moduleXml.exists()) {
			try {
				String contents = FileUtil.getContents(moduleXml);
				if( contents != null ) {
					XMLMemento memento = XMLMemento.createReadRoot(new FileInputStream(moduleXml));
					if( memento != null ) {
						String nodeName = memento.getNodeName();
						if( "module-alias".equals(nodeName)) { //$NON-NLS-1$
							String targetName = memento.getString("target-name"); //$NON-NLS-1$
							String targetSlot = memento.getString("target-slot");//$NON-NLS-1$
							
							if( !isEmpty(targetName)) {
								return isEmpty(targetSlot) ? new ModuleSlot(targetName) 
										: new ModuleSlot(targetName, targetSlot);
							}
						}
					}
				}
				
			} catch(IOException ce) {
				// Ignore
			}
		}
		return null;
	}
	
	private boolean isEmpty(String s) {
		return s == null || s.length() == 0;
	}
}
