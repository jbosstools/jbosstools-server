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
package org.jboss.ide.eclipse.as.core.publishers.patterns;

import org.eclipse.wst.server.core.model.IModuleResource;
import org.jboss.ide.eclipse.as.core.server.IModulePathFilter;

/**
 * This class is a default implementation for two of the three IModulePathFilter
 * methods. They are standard implementations of getFilteredMembers(etc) and 
 * getFilteredDelta(etc).  
 * 
 * This class promises to use only {@link IModulePathFilter#shouldInclude(IModuleResource)}
 * to evaluate whether an item should be included or not. 
 * 
 * IModulePathFilter implementors are not required to use this utility class
 * if they have more efficient ways of acquiring the same information.
 * 
 * This class performs no caching at all, so any client using it
 * should be sure to cache any results that may be requested often.
 * 
 * @since 3.0
 * @deprecated - please use superclass
 */
@Deprecated
public class ModulePathFilterUtility extends org.jboss.ide.eclipse.as.wtp.core.modules.filter.patterns.ModulePathFilterUtility {
	public ModulePathFilterUtility(IModulePathFilter filter) {
		super(filter);
	}
}
