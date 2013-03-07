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
package org.jboss.ide.eclipse.as.ui;

import org.eclipse.debug.ui.ILaunchConfigurationTab;

/**
 * AN extracted interface from a previously nested class. 
 * Simply return a number of ILaunchConfigurationTab objects. 
 */
public interface IJBossLaunchTabProvider {
	public ILaunchConfigurationTab[] createTabs();
}
