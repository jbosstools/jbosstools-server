/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.core.eap.xp;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.m2e.actions.MavenLaunchConstants;

/**
 * @author Red Hat Developers
 *
 */
public class MavenToolSupport {

	public static ILaunchConfigurationWorkingCopy getConfiguration(IProject project) throws CoreException {
		ILaunchConfigurationType launchConfigurationType = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(MavenLaunchConstants.LAUNCH_CONFIGURATION_TYPE_ID);
		ILaunchConfigurationWorkingCopy launchConfiguration = launchConfigurationType.newInstance(null, project.getName() + "__Maven__");
		launchConfiguration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, "${workspace_loc:/" + project.getName() + "}");
		launchConfiguration.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, new HashMap<>());
		launchConfiguration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH, ProjectUtils.getJREEntry(project));
		launchConfiguration.setAttribute(MavenLaunchConstants.ATTR_WORKSPACE_RESOLUTION, true);
		return launchConfiguration;
	}

	public static ILaunch run(IProject project, String profile, int debugPort, IProgressMonitor monitor) throws CoreException {
		ILaunchConfigurationWorkingCopy launchConfiguration = getConfiguration(project);
		launchConfiguration.setAttribute(MavenLaunchConstants.ATTR_GOALS, "package quarkus:dev");
		if (StringUtils.isBlank(profile)) {
			launchConfiguration.setAttribute(MavenLaunchConstants.ATTR_PROPERTIES, Collections.singletonList("debug=" + debugPort));
		} else {
			launchConfiguration.setAttribute(MavenLaunchConstants.ATTR_PROPERTIES, Arrays.asList("debug=" + debugPort, "quarkus.profile=" + profile));
		}
		return launchConfiguration.launch(ILaunchManager.RUN_MODE, monitor);
	}
}
