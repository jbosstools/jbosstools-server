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
package org.jboss.tools.openshift.express.internal.ui.wizard;

import java.util.Collection;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;
import org.jboss.tools.common.ui.WizardUtils;
import org.jboss.tools.common.ui.databinding.DataBindingUtils;
import org.jboss.tools.openshift.express.client.IApplication;
import org.jboss.tools.openshift.express.client.OpenshiftException;
import org.jboss.tools.openshift.express.internal.ui.OpenshiftUIActivator;

/**
 * @author André Dietisheim
 */
public class ApplicationWizardPage extends AbstractOpenshiftWizardPage {

	private TableViewer viewer;
	private ApplicationWizardPageModel model;

	protected ApplicationWizardPage(IWizard wizard, ServerAdapterWizardModel wizardModel) {
		super("Application selection", "Please select an Openshift Express application",
				"Application selection", wizard);
		this.model = new ApplicationWizardPageModel(wizardModel);
	}

	@Override
	protected void doCreateControls(Composite container, DataBindingContext dbc) {
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(container);

		Group group = new Group(container, SWT.BORDER);
		group.setText("Available Applications");
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).hint(400, 160).span(3, 1)
				.applyTo(group);
		FillLayout fillLayout = new FillLayout(SWT.VERTICAL);
		fillLayout.marginHeight = 6;
		fillLayout.marginWidth = 6;
		group.setLayout(fillLayout);

		Composite tableContainer = new Composite(group, SWT.NONE);
		this.viewer = createTable(tableContainer);
		viewer.addDoubleClickListener(onApplicationDoubleClick());

		Binding selectedApplicationBinding = dbc.bindValue(
				ViewerProperties.singleSelection().observe(viewer),
				BeanProperties.value(ApplicationWizardPageModel.PROPERTY_SELECTED_APPLICATION).observe(model),
				new UpdateValueStrategy().setAfterGetValidator(new IValidator() {

					@Override
					public IStatus validate(Object value) {
						if (value != null) {
							return ValidationStatus.ok();
						}
						else {
							return ValidationStatus.info("You have to select an application...");
						}
					}
				}),
				null);

		Button newButton = new Button(container, SWT.PUSH);
		newButton.setText("Ne&w");
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).hint(80, 30).applyTo(newButton);
		newButton.addSelectionListener(onNew(dbc));

		Button deleteButton = new Button(container, SWT.PUSH);
		deleteButton.setText("&Delete");
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).hint(80, SWT.DEFAULT).applyTo(deleteButton);
		DataBindingUtils.bindEnablementToValidationStatus(deleteButton, IStatus.INFO, dbc, selectedApplicationBinding);
		deleteButton.addSelectionListener(onDelete(dbc));

		Button detailsButton = new Button(container, SWT.PUSH);
		detailsButton.setText("De&tails");
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).hint(80, SWT.DEFAULT).applyTo(detailsButton);
		DataBindingUtils.bindEnablementToValidationStatus(detailsButton, IStatus.INFO, dbc , selectedApplicationBinding);
		detailsButton.addSelectionListener(onDetails(dbc));
	}

	private IDoubleClickListener onApplicationDoubleClick() {
		return new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
//				try {
//					ISelection selection = event.getSelection();
//					if (selection instanceof StructuredSelection) {
//						Object firstElement = ((IStructuredSelection) selection).getFirstElement();
//						if (firstElement instanceof IApplication) {
//							String url = ((IApplication) firstElement).getApplicationUrl();
//							BrowserUtil.checkedCreateExternalBrowser(url, OpenshiftUIActivator.PLUGIN_ID,
//									OpenshiftUIActivator.getDefault().getLog());
//						}
//					}
//				} catch (OpenshiftException e) {
//					IStatus status = new Status(IStatus.ERROR, OpenshiftUIActivator.PLUGIN_ID,
//							"Could not open Openshift Express application in browser", e);
//					OpenshiftUIActivator.getDefault().getLog().log(status);
//				}
				openDetailsDialog();
			}
		};
	}

	protected TableViewer createTable(Composite tableContainer) {
		Table table = new Table(tableContainer, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(table);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		TableColumnLayout tableLayout = new TableColumnLayout();
		tableContainer.setLayout(tableLayout);
		TableViewer viewer = new TableViewer(table);
		viewer.setContentProvider(new ArrayContentProvider());

		createTableColumn("Name", 1, new CellLabelProvider() {

			@Override
			public void update(ViewerCell cell) {
				IApplication application = (IApplication) cell.getElement();
				cell.setText(application.getName());
			}
		}, viewer, tableLayout);
		createTableColumn("Type", 1, new CellLabelProvider() {

			@Override
			public void update(ViewerCell cell) {
				IApplication application = (IApplication) cell.getElement();
				cell.setText(application.getCartridge().getName());
			}
		}, viewer, tableLayout);
//		createTableColumn("URL", 3, new CellLabelProvider() {
//
//			@Override
//			public void update(ViewerCell cell) {
//				try {
//					IApplication application = (IApplication) cell.getElement();
//					cell.setText(application.getApplicationUrl());
//				} catch (OpenshiftException e) {
//					// ignore
//				}
//			}
//		}, viewer, tableLayout);
		return viewer;
	}

	private void createTableColumn(String name, int weight, CellLabelProvider cellLabelProvider, TableViewer viewer,
			TableColumnLayout layout) {
		TableViewerColumn column = new TableViewerColumn(viewer, SWT.LEFT);
		column.getColumn().setText(name);
		column.setLabelProvider(cellLabelProvider);

		layout.setColumnData(column.getColumn(), new ColumnWeightData(weight, true));
	}

	private SelectionAdapter onDelete(final DataBindingContext dbc) {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					WizardUtils.runInWizard(new DeleteApplicationJob(), getWizard().getContainer(), dbc);
				} catch (Exception ex) {
					// ignore
				}
			}
		};
	}

	private SelectionAdapter onNew(DataBindingContext dbc) {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				Shell shell = getContainer().getShell();
				if (WizardUtils.openWizardDialog(new NewApplicationDialog(model.getUser()), shell)
						== Dialog.OK) {
					viewer.refresh();
				}
			}
		};
	}

	private SelectionAdapter onDetails(DataBindingContext dbc) {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				openDetailsDialog();
			}
		};
	}

	private void openDetailsDialog() {
		Shell shell = getContainer().getShell();
		new ApplicationDetailsDialog(model.getSelectedApplication(), shell).open();
	}
	
	@Override
	protected void onPageActivated(DataBindingContext dbc) {
		try {
			WizardUtils.runInWizard(new LoadApplicationsJob(), getWizard().getContainer(), dbc);
		} catch (Exception ex) {
			// ignore
		}
	}

	private class LoadApplicationsJob extends Job {
		private LoadApplicationsJob() {
			super("Loading applications");
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				final Collection<IApplication> applications = model.getApplications();
				Display display = PlatformUI.getWorkbench().getDisplay();
				display.syncExec(new Runnable() {

					@Override
					public void run() {
						viewer.setInput(applications);
					}
				});
				return Status.OK_STATUS;
			} catch (OpenshiftException e) {
				return new Status(IStatus.ERROR, OpenshiftUIActivator.PLUGIN_ID,
						"Could not load applications from Openshift Express");
			}
		}
	}

	private class DeleteApplicationJob extends Job {

		public DeleteApplicationJob() {
			super("Deleting application");
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				model.destroyCurrentApplication();
				getContainer().getShell().getDisplay().syncExec(new Runnable() {
					
					@Override
					public void run() {
						viewer.refresh();
					}
				});
				return Status.OK_STATUS;
			} catch (OpenshiftException e) {
				return new Status(IStatus.ERROR, OpenshiftUIActivator.PLUGIN_ID, NLS.bind(
						"Could not delete application \"{0}\"",
						model.getSelectedApplication().getName()));
			}
		}

	}
}
