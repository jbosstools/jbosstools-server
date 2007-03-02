package org.jboss.ide.eclipse.as.ui.packages;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.jboss.ide.eclipse.as.core.packages.WarPackageType;
import org.jboss.ide.eclipse.packages.core.model.IPackage;
import org.jboss.ide.eclipse.packages.core.model.PackagesCore;
import org.jboss.ide.eclipse.packages.core.model.types.IPackageType;
import org.jboss.ide.eclipse.packages.ui.PackagesUIPlugin;
import org.jboss.ide.eclipse.packages.ui.providers.PackagesContentProvider;
import org.jboss.ide.eclipse.packages.ui.providers.PackagesLabelProvider;
import org.jboss.ide.eclipse.ui.wizards.WizardPageWithNotification;

public class WARInfoWizardPage extends WizardPageWithNotification {

	private Group webinfGroup, classesGroup, libGroup;
	private NewWARWizard wizard;
	private TreeViewer warPreview;
	private boolean hasCreated = false;
	public WARInfoWizardPage (NewWARWizard wizard) {
		super("WAR information", "WAR Information", PackagesUIPlugin.getImageDescriptor(PackagesUIPlugin.IMG_NEW_WAR_WIZARD));
		this.wizard = wizard;
	}
	
	public void createControl(Composite parent) {
		setMessage("Information for the setup of your WAR. \n" + 
				"Later, you can customize this packaging structure further.");
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new FormLayout());
		warPreview = new TreeViewer(main);
		warPreview.setLabelProvider(new PackagesLabelProvider());
		warPreview.setContentProvider(new PackagesContentProvider());
		FormData warPreviewData = new FormData();
		warPreviewData.left = new FormAttachment(0,5);
		warPreviewData.right = new FormAttachment(100,-5);
		warPreviewData.top = new FormAttachment(0,5);
		warPreviewData.bottom = new FormAttachment(100,-5);
		warPreview.getTree().setLayoutData(warPreviewData);
		setControl(main);
	}

	public boolean isPageComplete() {
		return true;
	}
    public void pageEntered(int button) {
    	if( !hasCreated ) {
    		addToPackage();
    		hasCreated = true;
    	}
    	fillWidgets(wizard.getPackage());
    }
    
    protected void addToPackage() {
    	// fill it
    	IPackageType type = PackagesCore.getPackageType("org.jboss.ide.eclipse.as.core.packages.warPackage");
    	if( type instanceof WarPackageType ) {
    		((WarPackageType)type).fillDefaultConfiguration(wizard.getProject(), wizard.getPackage(), new NullProgressMonitor());
    		System.out.println("filling package");
    	}
    }
    protected void fillWidgets(IPackage pkg) {
    	System.out.println("filling widgets");
    	warPreview.setInput(new IPackage[] {pkg});
    	warPreview.expandAll();
    }
    public void pageExited(int button) {}

}
