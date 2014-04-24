/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.jboss.ide.eclipse.as.wtp.core.ASWTPToolsPlugin;
/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class UserPrompter implements IUserPrompter {
	// A default / collectively used prompter
	private static UserPrompter DEFAULT;
	
	// Get the default prompter
	public static UserPrompter getDefaultPrompter() {
		if( DEFAULT == null ) {
			DEFAULT = new UserPrompter();
		}
		return DEFAULT;
	}
	
	
	private HashMap<Integer, ArrayList<IPromptHandler>> map = new HashMap<Integer, ArrayList<IPromptHandler>>();
	
	// Clients may instantiate their own prompter if they wish
	// to keep their handlers separate from others. 
	public UserPrompter() {
		// Do nothing
	}
	
	
	public void addPromptHandler(int code, IPromptHandler prompt) {
		ArrayList<IPromptHandler> l = map.get(code);
		if( l == null ) {
			l = new ArrayList<IPromptHandler>();
			map.put(code, l);
		}
		l.add(prompt);
	}
	public void removePromptHandler(int code, IPromptHandler prompt) {
		ArrayList<IPromptHandler> l = map.get(code);
		if( l != null ) {
			l.remove(prompt);
		}
	}
	
	public Object promptUser(int code, Object... data) {
		ArrayList<IPromptHandler> l = map.get(code);
		if( l != null ) {
			Iterator<IPromptHandler> it = l.iterator();
			while(it.hasNext()) {
				IPromptHandler iup = it.next();
				Object ret = iup.promptUser(code, data);
				if( ret != null )
					return ret;
			}
		}
		ASWTPToolsPlugin.log("Unable to prompt user for event code " + code, null); //$NON-NLS-1$
		return null;
	}
}
