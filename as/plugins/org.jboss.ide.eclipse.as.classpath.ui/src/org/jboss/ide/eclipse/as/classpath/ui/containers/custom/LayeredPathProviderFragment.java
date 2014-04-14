/*******************************************************************************
 * Copyright (c) 2011-2104 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.classpath.ui.containers.custom;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.jboss.ide.eclipse.as.classpath.core.runtime.path.internal.LayeredProductPathProvider;
import org.jboss.tools.foundation.ui.xpl.taskwizard.IWizardHandle;
import org.jboss.tools.foundation.ui.xpl.taskwizard.WizardFragment;

public class LayeredPathProviderFragment extends WizardFragment {
	private IWizardHandle handle;
	private Text moduleText, slotText;
	private String moduleId, slot;
	public boolean hasComposite() {
		return true;
	}

	/**
	 * Creates the composite associated with this fragment.
	 * This method is only called when hasComposite() returns true.
	 * 
	 * @param parent a parent composite
	 * @param handle a wizard handle
	 * @return the created composite
	 */
	public Composite createComposite(Composite parent, IWizardHandle handle) {
		setComplete(false);
		this.handle = handle;
		handle.setTitle("Create a JBoss modules classpath entry.");
		handle.setDescription("This classpath entry will search all available modules folders for the chosen module." + 
		"This ensures patches are picked up properly. Example: module name = javax.faces.api,  slot=1.2");
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout(2, false));
		
		Label moduleLabel = new Label(c, SWT.NONE);
		moduleLabel.setText("Module ID: ");
		moduleText = new Text(c, SWT.SINGLE | SWT.BORDER);

		Label slotLabel = new Label(c, SWT.NONE);
		slotLabel.setText("Slot: ");
		slotText = new Text(c, SWT.SINGLE | SWT.BORDER);
		slotText.setText("main");

		GridData gd = new GridData(SWT.BEGINNING, SWT.CENTER, true, false);
		gd.widthHint = 200;
		moduleText.setLayoutData(gd);
		slotText.setLayoutData(gd);
		
		ModifyListener ml = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				moduleId = (moduleText == null ? null : moduleText.getText());
				setComplete(moduleId != null && moduleId.length() > 0);
				LayeredPathProviderFragment.this.handle.update();
			}
		};
		moduleText.addModifyListener(ml);
		
		ModifyListener slotml = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				slot = (slotText == null ? null : slotText.getText());
				LayeredPathProviderFragment.this.handle.update();
			}
		};
		slotText.addModifyListener(slotml);
		LayeredPathProviderFragment.this.handle.update();
		return c;
	}
	

	public void performFinish(IProgressMonitor monitor) throws CoreException {
		LayeredProductPathProvider prov = new LayeredProductPathProvider(moduleId, slot); //$NON-NLS-1$
		getTaskModel().putObject(RuntimeClasspathProviderWizard.CREATED_PATH_PROVIDER, prov);
	}
	
}
