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
package org.jboss.ide.eclipse.as.ui.editor.internal;

import org.eclipse.swt.widgets.Composite;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ISubsystemController;

public class DeployOnlyDeploymentPageController extends
		StandardDeploymentPageController implements ISubsystemController {

	public DeployOnlyDeploymentPageController() {
		super();
	}

	protected JBossDeploymentOptionsComposite createServerDeploymentOptions(Composite parent) {
		return new JBossDeploymentOptionsComposite(parent, this) {
			// Don't show radios for deploy-only server
			protected boolean getShowRadios() {
				return true;
			}
		};
	}
	
}
