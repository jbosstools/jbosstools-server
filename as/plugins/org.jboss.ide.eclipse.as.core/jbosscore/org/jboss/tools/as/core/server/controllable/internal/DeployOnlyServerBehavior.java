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
package org.jboss.tools.as.core.server.controllable.internal;

import org.eclipse.wst.server.core.IModule;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllableServerBehavior;

public class DeployOnlyServerBehavior extends ControllableServerBehavior {
	@Override
	protected boolean shouldIgnorePublishRequest(IModule m) {
		return (m.getProject() != null && m.getProject().exists() && !m.getProject().isAccessible());
	}

}
