package org.jboss.ide.eclipse.as.ui.packages;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.jboss.ide.eclipse.archives.core.model.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveType;
import org.jboss.ide.eclipse.archives.ui.ArchivesSharedImages;
import org.jboss.ide.eclipse.archives.ui.actions.ActionWithDelegate;
import org.jboss.ide.eclipse.archives.ui.views.ProjectArchivesView;
import org.jboss.ide.eclipse.archives.ui.wizards.AbstractArchiveWizard;
import org.jboss.ide.eclipse.as.core.packages.types.WarArchiveType;

public class NewWARAction extends ActionWithDelegate implements IViewActionDelegate {

	public void run() {
		NewWARWizard wizard = new NewWARWizard();
		wizard.init(PlatformUI.getWorkbench(), ProjectArchivesView.getInstance().getSelection());
		WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
		dialog.open();
	}
	
	public ImageDescriptor getImageDescriptor() {
		return ArchivesSharedImages.getImageDescriptor(ArchivesSharedImages.IMG_WAR);
	}
	
	public String getText() {
		return "WAR";
	}
	
	public String getToolTipText() {
		return "Create a new WAR archive";
	}
	
	public void init(IViewPart view) {
		
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
			pkg.setArchiveType(ArchivesCore.getArchiveType(WarArchiveType.WAR_PACKAGE_TYPE));
			return true;
		}
		
		public String getArchiveExtension() {
			return "war";
		}
	}

	protected static class WarPreviewPage extends PreviewPage {
		protected WarPreviewPage(NewWARWizard wiz) {
			super(wiz, "WAR information", "WAR Information", 
					ArchivesSharedImages.getImageDescriptor(ArchivesSharedImages.IMG_NEW_WAR_WIZARD));
		}
		protected void addToPackage() {
	    	IArchiveType type = ArchivesCore.getArchiveType(WarArchiveType.WAR_PACKAGE_TYPE);
    		type.fillDefaultConfiguration(wizard.getProject(), wizard.getArchive(), new NullProgressMonitor());
		}

		protected String getDescriptionMessage() {
			return 	"Information for the setup of your WAR. \n" + 
				"Later, you can customize this packaging structure further.";
		}
	}

	public IStructuredSelection getSelection() {
		return ProjectArchivesView.getInstance().getSelection();
	}

}
