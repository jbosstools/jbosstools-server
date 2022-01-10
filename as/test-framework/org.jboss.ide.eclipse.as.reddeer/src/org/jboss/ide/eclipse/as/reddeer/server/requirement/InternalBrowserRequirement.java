/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.ide.eclipse.as.reddeer.server.requirement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.junit.requirement.AbstractRequirement;
import org.eclipse.reddeer.swt.condition.ShellIsAvailable;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.eclipse.reddeer.workbench.ui.dialogs.WorkbenchPreferenceDialog;
import org.jboss.ide.eclipse.as.preferences.WebBrowserPreferencePage;
import org.jboss.ide.eclipse.as.reddeer.server.requirement.InternalBrowserRequirement.UseInternalBrowser;

/**
 * RedDeer Requirement that allows to setup an Internal Browser option as default 
 * in Preferences: General -> Web Browser.
 * 
 * @author odockal
 *
 */
public class InternalBrowserRequirement extends AbstractRequirement<UseInternalBrowser>{
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface UseInternalBrowser {
		
	}

	@Override
	public void fulfill() {
		WorkbenchPreferenceDialog dialog = new WorkbenchPreferenceDialog();
		WebBrowserPreferencePage page = new WebBrowserPreferencePage(dialog, new String[]{"General", "Web Browser"});
		
		dialog.open();
		dialog.select(page);
		page.toggleInternalBrowser();

		dialog.ok();
		new WaitWhile(new JobIsRunning());
		new WaitWhile(new ShellIsAvailable(dialog.getShell()));
	}

	@Override
	public void cleanUp() {
	}

}
