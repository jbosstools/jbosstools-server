/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.archives.webtools.ui;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.ui.actions.INodeActionDelegate;

public class EditPublishAction implements INodeActionDelegate {

	public boolean isEnabledFor(IArchiveNode node) {
		if( node.getNodeType() == IArchiveNode.TYPE_ARCHIVE) 
			return true;
		return false;
	}

	public void run(IArchiveNode node) {
		ArchivePublishWizard wiz = new ArchivePublishWizard((IArchive)node);
		new WizardDialog(new Shell(), wiz).open();
	}

}
