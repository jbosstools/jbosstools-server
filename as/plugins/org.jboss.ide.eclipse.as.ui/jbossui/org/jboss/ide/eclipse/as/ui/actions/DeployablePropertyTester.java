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
package org.jboss.ide.eclipse.as.ui.actions;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IResource;
import org.jboss.ide.eclipse.as.core.modules.SingleDeployableFactory;

public class DeployablePropertyTester extends PropertyTester {

	@Override
	/**
	 * method checks if IResource is deployable
	 * 
	 * @parameters
	 * receiver - IResource
	 * property - not using
	 * expectedValue - not using
	 */
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
			if( !(receiver instanceof IResource) || SingleDeployableFactory.findModule(((IResource)receiver).getFullPath()) == null)
				return true;
		return false;
	}

}
