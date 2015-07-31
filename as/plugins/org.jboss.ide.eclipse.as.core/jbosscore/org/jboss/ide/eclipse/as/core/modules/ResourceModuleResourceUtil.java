/******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.modules;

import org.jboss.ide.eclipse.as.wtp.core.modules.filter.patterns.ComponentModuleInclusionFilterUtility;


/**
 * This class is intended  for use to discover includes and excludes patterns
 * from a component-core (wtp-style) project. It is then used 
 * to filter the members and return a clean post-filter module resource tree
 * which can be used during publish. 
 * 
 * This class has absolutely no reason to extend ModuleResourceUtil
 * and this should be changed!!
 * 
 * @deprecated Use superclass
 */
@Deprecated
public class ResourceModuleResourceUtil  extends ComponentModuleInclusionFilterUtility {

}
