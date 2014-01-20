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
package org.jboss.ide.eclipse.as.ui.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.internal.RecentlyUpdatedServerLaunches;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.subsystems.IJBossLaunchTabProvider;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class JBossLaunchConfigurationTabGroup extends
		AbstractLaunchConfigurationTabGroup {
	
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		IServer s = RecentlyUpdatedServerLaunches.getDefault().getRecentServer();
		if( s != null ) {
			IControllableServerBehavior beh = JBossServerBehaviorUtils.getControllableBehavior(s);
			if( beh != null ) {
				try {
					IJBossLaunchTabProvider cont = (IJBossLaunchTabProvider)beh.getController(IJBossLaunchTabProvider.SYSTEM_ID);
					ILaunchConfigurationTab[] tabs = cont.createTabs();
					for( int j = 0; j < tabs.length; j++ ) {
						tabs[j].setLaunchConfigurationDialog(dialog);
					}
					setTabs(tabs);
					return;
				} catch(CoreException ce) {
					JBossServerUIPlugin.log(ce.getStatus());
				}
			}
		}
		setTabs(new ILaunchConfigurationTab[]{});
	}
	
	public static Composite createComposite(Composite parent, Font font, int columns, int hspan, int fill) {
    	Composite g = new Composite(parent, SWT.NONE);
    	g.setLayout(new GridLayout(columns, false));
    	g.setFont(font);
    	GridData gd = new GridData(fill);
		gd.horizontalSpan = hspan;
    	g.setLayoutData(gd);
    	return g;
    }
}
