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
import org.jboss.ide.eclipse.as.core.extensions.archives.EjbArchiveType;

public class NewEJBJARAction extends ActionWithDelegate implements IViewActionDelegate {

	public void run() {
		AbstractArchiveWizard wizard = new NewEJBWizard();
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
		return "EJB JAR";
	}
	
	public String getToolTipText() {
		return "Create a new EJB JAR archive";
	}
	
	public void init(IViewPart view) {
		
	}

	public static class NewEJBWizard extends AbstractArchiveWizard {

		private EjbJarPreviewPage ejbInfoPage;
		
		public NewEJBWizard () {
			setWindowTitle("New EJB JAR");
		}
		
		public ImageDescriptor getImageDescriptor() {
			return ArchivesSharedImages.getImageDescriptor(ArchivesSharedImages.IMG_EJB_JAR);
		}

		public WizardPage[] createWizardPages() {
			ejbInfoPage = new EjbJarPreviewPage(this);
			
			return new WizardPage[] { ejbInfoPage };
		}

		public boolean performFinish(IArchive pkg) {
			pkg.setArchiveType(ArchivesCore.getInstance().getExtensionManager().getArchiveType(EjbArchiveType.ID));
			return true;
		}
		
		public String getArchiveExtension() {
			return "jar";
		}
	}


	protected static class EjbJarPreviewPage extends PreviewPage {
		protected EjbJarPreviewPage(NewEJBWizard wiz) {
			super(wiz, "EJB JAR Preview", "EJB JAR Preview", 
					ArchivesSharedImages.getImageDescriptor(ArchivesSharedImages.IMG_NEW_JAR_WIZARD));
		}
		protected void addToPackage() {
	    	IArchiveType type = ArchivesCore.getInstance().getExtensionManager().getArchiveType(EjbArchiveType.ID);
    		type.fillDefaultConfiguration(wizard.getProject().getName(), wizard.getArchive(), new NullProgressMonitor());
		}

		protected String getDescriptionMessage() {
			return 	"Preview the EJB JAR \n" + 
				"Later, you can customize this structure further.";
		}
	}

}
