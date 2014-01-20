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
package org.jboss.ide.eclipse.as.ui.subsystems.internal;

import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wst.server.core.IServerAttributes;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;
import org.jboss.ide.eclipse.as.ui.subsystems.IBrowseBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;

public class LocalBrowseBehavior extends AbstractSubsystemController implements IBrowseBehavior {
	public String openBrowseDialog(IServerAttributes server, String original) {
		DirectoryDialog d = new DirectoryDialog(new Shell());
		String filterPath = ServerUtil.makeGlobal(server.getRuntime(), new Path(original)).toString();
		d.setFilterPath(filterPath);
		return d.open();
	}
}
