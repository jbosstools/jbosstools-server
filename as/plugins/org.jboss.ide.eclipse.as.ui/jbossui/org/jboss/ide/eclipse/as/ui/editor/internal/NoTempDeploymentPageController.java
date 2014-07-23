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
import org.jboss.ide.eclipse.as.ui.editor.ModuleDeploymentOptionsComposite;

/**
 * A controller which does not offer options for temporary folders. 
 * This may be used by rse, or others who know for certain they
 * will not be using a temporary deploy folder
 * @author rob
 *
 */
public class NoTempDeploymentPageController extends
		StandardDeploymentPageController {

	protected JBossDeploymentOptionsComposite createServerDeploymentOptions(Composite parent) {
		return new JBossDeploymentOptionsComposite(parent, this) {
			protected boolean showTempDeployText() {
				return false;
			}
		};
	}
	
	protected ModuleDeploymentOptionsComposite createModuleDeploymentOptions(Composite parent) {
		return new ModuleDeploymentOptionsComposite(parent, getPage(), getFormToolkit(parent), getPage().getPreferences()) {
			protected boolean showTemporaryColumn() {
				return false;
			}
		};
	}

}
