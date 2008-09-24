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
package org.jboss.ide.eclipse.archives.ui.wizards.pages;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.jboss.ide.eclipse.archives.ui.ArchivesSharedImages;
import org.jboss.ide.eclipse.archives.ui.ArchivesUIMessages;
import org.jboss.ide.eclipse.archives.ui.providers.ArchivesContentProviderDelegate;
import org.jboss.ide.eclipse.archives.ui.providers.ArchivesLabelProvider;
import org.jboss.ide.eclipse.archives.ui.wizards.AbstractArchiveWizard;
import org.jboss.ide.eclipse.archives.ui.wizards.WizardPageWithNotification;

/**
 *
 * @author "Rob Stryker" <rob.stryker@redhat.com>
 *
 */
public class DefaultJARConfigWizardPage extends WizardPageWithNotification {

	private AbstractArchiveWizard wizard;

	public DefaultJARConfigWizardPage (AbstractArchiveWizard wizard) {
		super (ArchivesUIMessages.DefaultJarConfiguration,
				ArchivesUIMessages.DefaultJarConfiguration,
				ArchivesSharedImages.getImageDescriptor(ArchivesSharedImages.IMG_NEW_JAR_WIZARD));

		this.wizard = wizard;
	}

	public void createControl(Composite parent) {

		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(1, false));

		Button createDefaultConfig = new Button(main, SWT.CHECK);
		createDefaultConfig.setText(ArchivesUIMessages.UseDefaultJARConfiguration);

		new Label(main, SWT.NONE).setText(ArchivesUIMessages.Preview);

		TreeViewer previewTree = new TreeViewer(main);
		previewTree.setContentProvider(new ArchivesContentProviderDelegate(false));
		previewTree.setLabelProvider(new ArchivesLabelProvider());
		previewTree.setInput(wizard.getArchive());

		setControl(main);
	}

}
