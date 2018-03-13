/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.runtimes.integration.ui.composites;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.jboss.ide.eclipse.as.wtp.ui.composites.RuntimeHomeComposite;
import org.jboss.ide.eclipse.as.wtp.ui.util.FormDataUtility;

public class DownloadRuntimeHomeComposite extends RuntimeHomeComposite {
	
	protected DownloadRuntimeHyperlinkComposite hyperlink;
	public DownloadRuntimeHomeComposite(Composite parent, int style, IWizardHandle handle, TaskModel tm) {
		super(parent, style, handle, tm);
	}
	
	protected void createWidgets() {
		super.createWidgets();
		hyperlink = new DownloadRuntimeHyperlinkComposite(this, SWT.NONE, getWizardHandle(), getTaskModel()) {
			
			@Override
			protected void postDownloadRuntimeUpdateWizard(String newHomeDir) {
				if (!homeDirText.isDisposed())
					homeDirText.setText(newHomeDir);
				getWizardHandle().update();
			}
		};
		hyperlink.setLayoutData(FormDataUtility.createFormData2(0,0,homeDirButton,-5,null,0,100,-10));
	}
}
