/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.tools.as.core.server.controllable.systems;

import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IServerShutdownController;

/**
 * This is a marker interface used to designate that this shutdown controller
 * is one that issues commands to run on either a local or remote shell, 
 * rather than one that handles the shutdown task via some other API such as management calls. 
 */
public interface ICommandLineShutdownController extends IServerShutdownController {

}
