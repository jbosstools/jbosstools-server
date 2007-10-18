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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveType;
import org.jboss.ide.eclipse.archives.ui.ArchivesSharedImages;
import org.jboss.ide.eclipse.archives.ui.actions.ActionWithDelegate;
import org.jboss.ide.eclipse.archives.ui.views.ProjectArchivesView;
import org.jboss.ide.eclipse.archives.ui.wizards.AbstractArchiveWizard;
import org.jboss.ide.eclipse.as.core.extensions.archives.EarArchiveType;

public class NewEARAction extends ActionWithDelegate {

	public void run() {
		AbstractArchiveWizard wizard = new NewEARWizard();
		wizard.init(PlatformUI.getWorkbench(), ProjectArchivesView.getInstance().getSelection());
		WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
		dialog.open();
	}
	public IStructuredSelection getSelection() {
		return ProjectArchivesView.getInstance().getSelection();
	}

	
	public ImageDescriptor getImageDescriptor() {
		return ArchivesSharedImages.getImageDescriptor(ArchivesSharedImages.IMG_EJB_JAR);
	}
	
	public String getText() {
		return "EAR";
	}
	
	public String getToolTipText() {
		return "Create a new EAR archive";
	}
	
	public void init(IViewPart view) {
		
	}

	public static class NewEARWizard extends AbstractArchiveWizard {

		private EARPreviewPage earInfoPage;
		
		public NewEARWizard () {
			setWindowTitle("New EAR");
		}
		
		public ImageDescriptor getImageDescriptor() {
			return ArchivesSharedImages.getImageDescriptor(ArchivesSharedImages.IMG_EAR);
		}

		public WizardPage[] createWizardPages() {
			earInfoPage = new EARPreviewPage(this);
			
			return new WizardPage[] { earInfoPage };
		}

		public boolean performFinish(IArchive pkg) {
			pkg.setArchiveType(ArchivesCore.getInstance().getExtensionManager().getArchiveType(EarArchiveType.ID));
			return true;
		}
		
		public String getArchiveExtension() {
			return "ear";
		}
	}


	protected static class EARPreviewPage extends PreviewPage {
		protected EARPreviewPage(NewEARWizard wiz) {
			super(wiz, "EAR Preview", "EAR Preview", 
					ArchivesSharedImages.getImageDescriptor(ArchivesSharedImages.IMG_EAR));
		}
		protected void addToPackage() {
	    	IArchiveType type = ArchivesCore.getInstance().getExtensionManager().getArchiveType(EarArchiveType.ID);
    		type.fillDefaultConfiguration(wizard.getProject().getName(), wizard.getArchive(), new NullProgressMonitor());
		}

		protected String getDescriptionMessage() {
			return 	"Preview the EAR\n" + 
				"Later, you can customize this structure further.";
		}
	}
}
