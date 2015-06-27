/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.internal.management.wildfly8;

import org.jboss.ide.eclipse.as.management.core.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManagerUtil;
import org.jboss.ide.eclipse.as.management.core.service.DelegatingManagerService;

/**
 * The wf8 service simply wraps the wf9 service.
 * For better efficiency, clients should change their requests
 * to the wf9 service directly. 
 * This service is left around for legacy clients who may have
 * hard-coded their service version.
 */
public class Wildfly8ManagerService extends DelegatingManagerService {
	protected String getDelegateServiceId() {
		return IJBoss7ManagerService.WILDFLY_VERSION_900;
	}
}
