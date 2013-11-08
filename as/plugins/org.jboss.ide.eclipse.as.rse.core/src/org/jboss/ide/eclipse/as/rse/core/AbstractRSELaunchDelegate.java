/******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 * 
 * TODO: Logging and Progress Monitors
 ******************************************************************************/
package org.jboss.ide.eclipse.as.rse.core;

import org.jboss.ide.eclipse.as.core.server.IJBossLaunchDelegate;


/**
 * Since there is only 1 direct subclass, these methods have been moved down 
 * to assist in removing an unnecessary class.
 * 
 * See {@link RSEJBossStartLaunchDelegate}
 */
@Deprecated
public abstract class AbstractRSELaunchDelegate implements IJBossLaunchDelegate {
}
