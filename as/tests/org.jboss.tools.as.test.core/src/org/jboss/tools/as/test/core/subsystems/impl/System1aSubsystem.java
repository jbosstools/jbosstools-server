/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.test.core.subsystems.impl;

import org.eclipse.core.runtime.CoreException;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ISubsystemController;

// Simple controller which extends abstract controller
public class System1aSubsystem extends AbstractSubsystemController {
	@Override
	public ISubsystemController findDependency(String system, String serverType) throws CoreException {
		return super.findDependency(system, serverType);
	}

}
