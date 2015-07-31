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

package org.jboss.ide.eclipse.as.core.publishers.patterns.internal;

import org.eclipse.wst.server.core.model.IModuleResource;

/**
 * Class for scanning the servertools IModuleResource model
 * for IModuleResource elements which match certain
 * criteria.  Please see superclass for detailed information
 * about the syntax of inclusion and exclusion. 
 * <p>
 * Example of usage:
 * <pre>
 *   IModule mod = {some module};
 *   ModuleDelegate del = (ModuleDelegate)mod.loadAdapter(ModuleDelegate.class, null);
 *   PublishFilterDirectoryScanner scanner = new PublishFilterDirectoryScanner(del.members());
 *   String[] includes = {"**\\*.class"};
 *   String[] excludes = {"modules\\*\\**"};
 *   ds.setIncludes(includes);
 *   ds.setExcludes(excludes);
 *   scanner.scan();
 *   IModuleResource[] trimmed = scanner.getCleanedMembers();
 *   
 * </pre>
 * This will return an IModuleResource model which is a clone 
 * of the original minus any resources that should be excluded. 
 * @since 2.3
 *
 */

/**
 * @since 2.3
 * @deprecated - please use superclass
 */
@Deprecated
public class PublishFilterDirectoryScanner extends org.jboss.ide.eclipse.as.wtp.core.modules.filter.patterns.internal.PublishFilterDirectoryScanner {
    public PublishFilterDirectoryScanner(IModuleResource[] resources) {
    	super(resources);
    }
}
