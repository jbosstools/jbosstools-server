/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.openshift.ui.internal.wizard;

import java.util.Collection;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;
import org.jboss.ide.eclipse.as.openshift.core.IApplication;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;
import org.jboss.ide.eclipse.as.openshift.ui.internal.OpenshiftUIActivator;
import org.jboss.tools.common.ui.WizardUtils;

/**
 * @author André Dietisheim
 */
public class ApplicationWizardPage extends AbstractOpenshiftWizardPage {

	private TableViewer viewer;
	private ApplicationWizardPageModel model;

	protected ApplicationWizardPage(IWizard wizard, ServerAdapterWizardModel wizardModel) {
		super("Application selection", "Please select an Openshift Express application to use",
				"Application selection", wizard);
		this.model = new ApplicationWizardPageModel(wizardModel);
	}

	@Override
	protected void doCreateControls(Composite container, DataBindingContext dbc) {
		GridLayoutFactory.fillDefaults().numColumns(1).margins(10, 10).applyTo(container);

		Group group = new Group(container, SWT.BORDER);
		group.setText("Available applications");
		GridDataFactory.fillDefaults().hint(600, 300).applyTo(group);
		FillLayout fillLayout = new FillLayout(SWT.VERTICAL);
		fillLayout.marginHeight = 10;
		fillLayout.marginWidth = 10;
		group.setLayout(fillLayout);
		
		Table table = new Table(group, SWT.BORDER | SWT.V_SCROLL);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		this.viewer = new TableViewer(table);
	}

	@Override
	protected void onPageActivated(DataBindingContext dbc) {
		try {
			WizardUtils.runInWizard(new LoadApplicationsJob(), getWizard().getContainer(), dbc);
		} catch (Exception ex) {
			// ignore
		}
	}

	protected void bindApplications(final Collection<IApplication> applications, final TableViewer viewer) {
		Display display = PlatformUI.getWorkbench().getDisplay();
		display.syncExec(new Runnable() {

			@Override
			public void run() {
				IObservableList input = new WritableList(applications, IApplication.class);
				ViewerSupport.bind(viewer, input, BeanProperties.values(new String[] { "name", "applicationUrl" }));
			}
		});
	}

	private class LoadApplicationsJob extends Job {
		private LoadApplicationsJob() {
			super("Loading applications");
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				bindApplications(model.getApplications(), viewer);
				return Status.OK_STATUS;
			} catch (OpenshiftException e) {
				return new Status(IStatus.ERROR, OpenshiftUIActivator.PLUGIN_ID,
						"Could not load applications from Openshift Express");
			}
		}
	}
}
