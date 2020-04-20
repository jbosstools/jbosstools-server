/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.ide.eclipse.as.reddeer.server.editor;

import org.jboss.ide.eclipse.as.reddeer.server.view.JBossServer;
import org.eclipse.reddeer.swt.impl.group.DefaultGroup;
import org.eclipse.reddeer.swt.impl.text.DefaultText;

/**
 * Represents a server editor's launch configuration  with entries specific 
 * for JBoss servers {@link JBossServer}
 * @author Lucia Jelinkova
 *
 */
public class JBossServerLaunchConfiguration {
	
	private JBossServerEditor editor;
	
	public JBossServerLaunchConfiguration(JBossServerEditor editor) {
		this.editor = editor;
	}

	public String getProgramArguments(){
		return new DefaultText(new DefaultGroup(editor, "Program arguments:")).getText();
	}
	
	public void setProgramArguments(String arguments){
		new DefaultText(new DefaultGroup(editor, "Program arguments:")).setText(arguments);
	}
	
	public String getVMArguments(){
		return new DefaultText(new DefaultGroup(editor, "VM arguments:")).getText();
	}
	
	public void setVMArguments(String arguments){
		new DefaultText(new DefaultGroup(editor, "VM arguments:")).setText(arguments);
	}
}
