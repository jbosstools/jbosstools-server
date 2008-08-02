/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.ui.packages;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveType;
import org.jboss.ide.eclipse.archives.ui.ArchivesSharedImages;
import org.jboss.ide.eclipse.archives.ui.wizards.AbstractArchiveWizard;
import org.jboss.ide.eclipse.as.core.extensions.archives.WarArchiveType;

public class NewWARAction implements IActionDelegate {
	private IStructuredSelection selection;
	public void run() {
		NewWARWizard wizard = new NewWARWizard();
		wizard.init(PlatformUI.getWorkbench(), selection);
		WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
		dialog.open();
	}

	public void run(IAction action) {
		run();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if( selection instanceof IStructuredSelection)
			this.selection = (IStructuredSelection)selection;
	}

	
	public static class NewWARWizard extends AbstractArchiveWizard {

		private WarPreviewPage warInfoPage;
		
		public NewWARWizard ()
		{
			setWindowTitle("New WAR");
		}
		
		public ImageDescriptor getImageDescriptor() {
			return null;
		}

		public WizardPage[] createWizardPages() {
			warInfoPage = new WarPreviewPage(this);
			
			return new WizardPage[] { warInfoPage };
		}

		public boolean performFinish(IArchive pkg) {
			pkg.setArchiveType(ArchivesCore.getInstance().getExtensionManager().getArchiveType(WarArchiveType.WAR_PACKAGE_TYPE));
			return true;
		}
		
		public String getArchiveExtension() {
			return "war";
		}
	}

	protected static class WarPreviewPage extends PreviewPage {
		protected WarPreviewPage(NewWARWizard wiz) {
			super(wiz, "WAR Archive Stub", "WAR Archive Stub", 
					ArchivesSharedImages.getImageDescriptor(ArchivesSharedImages.IMG_NEW_WAR_WIZARD));
		}
		protected void addToPackage() {
	    	IArchiveType type = ArchivesCore.getInstance().getExtensionManager().getArchiveType(WarArchiveType.WAR_PACKAGE_TYPE);
    		type.fillDefaultConfiguration(wizard.getProject().getName(), wizard.getArchive(), new NullProgressMonitor());
		}

		protected String getDescriptionMessage() {
			return 	"Below is a stub archive configuration for your project. \n" + 
				"You can customize this structure further after pressing finish.";
		}
	}
}
