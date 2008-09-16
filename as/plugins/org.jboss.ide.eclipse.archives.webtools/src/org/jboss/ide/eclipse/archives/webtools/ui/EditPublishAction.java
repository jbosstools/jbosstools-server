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
