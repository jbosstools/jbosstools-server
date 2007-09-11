package org.jboss.ide.eclipse.as.ui.packages;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveType;
import org.jboss.ide.eclipse.archives.ui.ArchivesSharedImages;
import org.jboss.ide.eclipse.archives.ui.actions.ActionWithDelegate;
import org.jboss.ide.eclipse.archives.ui.views.ProjectArchivesView;
import org.jboss.ide.eclipse.archives.ui.wizards.AbstractArchiveWizard;
import org.jboss.ide.eclipse.as.core.extensions.archives.WarArchiveType;

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

	public IStructuredSelection getSelection() {
		return ProjectArchivesView.getInstance().getSelection();
	}

}
