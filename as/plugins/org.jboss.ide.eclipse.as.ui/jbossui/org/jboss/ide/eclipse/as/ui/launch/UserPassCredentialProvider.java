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

import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.jboss.ide.eclipse.as.core.server.IJBossServer;
import org.jboss.ide.eclipse.as.core.server.INeedCredentials;
import org.jboss.ide.eclipse.as.core.server.IProvideCredentials;
import org.jboss.ide.eclipse.as.core.server.IServerProvider;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.dialogs.RequiredCredentialsDialog;
import org.osgi.service.prefs.BackingStoreException;

/**
 * 
 * @author rob.stryker <rob.stryker@redhat.com>
 *
 */
public class UserPassCredentialProvider implements IProvideCredentials {

	public static final String NEVER_ASK_AGAIN_KEY = "org.jboss.ide.eclipse.as.ui.launch.credentials.neverAsk"; //$NON-NLS-1$
	
	public boolean accepts(IServerProvider serverProvider,
			List<String> requiredProperties) {
		if( requiredProperties.size() > 2)
			return false;
		IServer s = serverProvider.getServer();
		IJBossServer jbs = ServerConverter.getJBossServer(s);
		if( jbs != null && !jbs.hasJMXProvider())
			return true;
		return false;
	}

	/**
	 * This class assumes that the first two required credentials will be 
	 * a simple user / pass combination 
	 * 
	 */
	public void handle(final INeedCredentials inNeed,
			final List<String> requiredProperties) {
		final IServer server = inNeed.getServer();
		final IServerWorkingCopy copy = server.createWorkingCopy();
		final JBossServer jbs = (JBossServer)ServerConverter.getJBossServer(copy);
		final String initialUser = jbs.getUsername();
		final String initialPass = jbs.getPassword();
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(JBossServerUIPlugin.PLUGIN_ID);
		boolean ignore = prefs.getBoolean(NEVER_ASK_AGAIN_KEY, false);
		if( ignore ) {
			Properties p = new Properties();
			p.put(requiredProperties.get(0), initialUser);
			p.put(requiredProperties.get(1), initialPass);
			inNeed.provideCredentials(p);
			return;
		}

		Display.getDefault().syncExec(new Runnable() { 
			public void run() {
				RequiredCredentialsDialog d = new RequiredCredentialsDialog(
						new Shell(), initialUser, initialPass);
				if( d.open() == Window.OK) {
					if( d.getNeverAskAgain() ) {
						// persist this setting 
						IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(JBossServerUIPlugin.PLUGIN_ID);
						prefs.putBoolean(NEVER_ASK_AGAIN_KEY, true);
						try {
							prefs.flush();
						} catch (BackingStoreException e) {
							JBossServerUIPlugin.log("Unable to save dialog settings", e);
						}
					}
					
					if( d.getSave() ) {
						jbs.setPassword(d.getPass());
						jbs.setUsername(d.getUser());
						try {
							copy.save(false, null);
						} catch( CoreException ce ) {
							JBossServerUIPlugin.log(Messages.ServerSaveFailed, ce);
						}
					}
					
					Properties p = new Properties();
					p.put(requiredProperties.get(0), d.getUser());
					p.put(requiredProperties.get(1), d.getPass());
					inNeed.provideCredentials(p);
				} else {
					inNeed.provideCredentials(null);
				}
			}
		});
	}
}
