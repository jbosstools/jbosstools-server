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
package org.jboss.ide.eclipse.as.ui.editor;

import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.jboss.ide.eclipse.as.ui.IBrowseBehavior;

public class LocalBrowseBehavior implements IBrowseBehavior {
	public String openBrowseDialog(ModuleDeploymentPage page, String original) {
		DirectoryDialog d = new DirectoryDialog(new Shell());
		d.setFilterPath(page.makeGlobal(original));
		return d.open();
	}
}
