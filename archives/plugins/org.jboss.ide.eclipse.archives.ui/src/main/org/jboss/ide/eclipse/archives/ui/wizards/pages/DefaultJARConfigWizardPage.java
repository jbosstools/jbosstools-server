package org.jboss.ide.eclipse.archives.ui.wizards.pages;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.jboss.ide.eclipse.archives.ui.ArchivesSharedImages;
import org.jboss.ide.eclipse.archives.ui.providers.ArchivesContentProviderDelegate;
import org.jboss.ide.eclipse.archives.ui.providers.ArchivesLabelProvider;
import org.jboss.ide.eclipse.archives.ui.wizards.AbstractArchiveWizard;
import org.jboss.ide.eclipse.archives.ui.wizards.WizardPageWithNotification;

public class DefaultJARConfigWizardPage extends WizardPageWithNotification {

	private AbstractArchiveWizard wizard;
	
	public DefaultJARConfigWizardPage (AbstractArchiveWizard wizard) {
		super ("Default JAR Configuration", "Default JAR Configuration",
				ArchivesSharedImages.getImageDescriptor(ArchivesSharedImages.IMG_NEW_JAR_WIZARD));
		
		this.wizard = wizard;
	}
	
	public void createControl(Composite parent) {

		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(1, false));
		
		Button createDefaultConfig = new Button(main, SWT.CHECK);
		createDefaultConfig.setText("Use default JAR configuration");
		
		new Label(main, SWT.NONE).setText("Preview");
		
		TreeViewer previewTree = new TreeViewer(main);
		previewTree.setContentProvider(new ArchivesContentProviderDelegate(false));
		previewTree.setLabelProvider(new ArchivesLabelProvider());
		previewTree.setInput(wizard.getArchive());
		
		setControl(main);
	}

}
