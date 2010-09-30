/******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.ui.editor;

import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServerWorkingCopy;

public interface IDeploymentTypeUI {
	
	public interface IServerModeUICallback {
		public IServerWorkingCopy getServer();
		public IRuntime getRuntime();
		public void execute(IUndoableOperation operation);
	}
	
	/**
	 * The parent in this call has no layout and is basically a positioned, 
	 * but unconfigured, composite. 
	 * 
	 * Fill her up!
	 * 
	 * Don't forget this UI element is a singleton, similar to a factory, 
	 * so you should probably make your first widget in the parent a
	 * new class which extends Composite and can maintain state.
	 * 
	 * @param parent
	 * @param modeSection
	 */
	@Deprecated
	public void fillComposite(Composite parent, ServerModeSection modeSection);
	
	public void fillComposite(Composite parent, IServerModeUICallback callback);
}
