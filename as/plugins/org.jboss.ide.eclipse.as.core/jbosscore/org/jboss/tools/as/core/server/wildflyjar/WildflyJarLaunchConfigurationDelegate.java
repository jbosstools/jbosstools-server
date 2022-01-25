/******************************************************************************* 
 * Copyright (c) 2021 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.as.core.server.wildflyjar;

import org.eclipse.m2e.internal.launch.MavenLaunchDelegate;

public class WildflyJarLaunchConfigurationDelegate extends MavenLaunchDelegate {
	public static final String ID = "org.jboss.tools.as.core.server.wildflyjar.WildflyJarLaunchConfiguration";
	public static final String DEFAULT_GOAL = "wildfly-jar:dev-watch";
}
