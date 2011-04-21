/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.management.as7.deployment;

import static org.jboss.ide.eclipse.as.management.as7.deployment.ModelDescriptionConstants.ENABLED;

import org.jboss.dmr.ModelNode;

/**
 * An enum that reflects the state of a deployment.
 * 
 * @author André Dietisheim
 */
public enum DeploymentState {
	STARTED {
		protected boolean matches(boolean enabled) {
			return enabled == true;
		}
	},
	STOPPED {
		protected boolean matches(boolean enabled) {
			return enabled == false;
		}
	};
	
	public static DeploymentState getForResultNode(ModelNode node) {
		Boolean enabled = AS7ManagerUtil.getBooleanProperty(ENABLED, node);
		if (enabled == null) {
			return null;
		}
		
		DeploymentState matchingState = null;
		for(DeploymentState state : values()) {
			if (state.matches(enabled)) {
				matchingState = state;
			}
		}
		return matchingState;
	}

	protected abstract boolean matches(boolean enabled);

}