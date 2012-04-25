/*******************************************************************************
 * Copyright (c) 2010 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.ui.wizards;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;

/**
 * @author Marshall
 */
public class JBossConfigurationTableViewer extends TableViewer {
	// private String jbossHome;
	private String selectedConfiguration;
	public JBossConfigurationTableViewer(Composite parent) {
		super(parent);
		init();
	}

	public JBossConfigurationTableViewer(Composite parent, int style) {
		super(parent, style);
		init();
	}

	public JBossConfigurationTableViewer(Table table) {
		super(table);
		init();
	}

	public void setFolder(String folder) {
		setInput(folder);
		String t = getSelectedConfiguration();
		if( t != null )
			setSelection(new StructuredSelection(new Object[]{t}));
	}

	public String getSelectedConfiguration() {
		return selectedConfiguration;
	}

	public void setConfiguration(String defaultConfiguration) {
		setSelection(new StructuredSelection(defaultConfiguration));
		selectedConfiguration = defaultConfiguration;
	}

	private void init() {
		ConfigurationProvider provider = new ConfigurationProvider();
		setContentProvider(provider);
		setLabelProvider(provider);
		getControl().setLayoutData(
				new GridData(GridData.FILL, GridData.FILL, true, true));
		addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				configurationSelected();
			}
		});
	}

	protected String getCurrentlySelectedConfiguration() {
		if (getSelection() instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) getSelection();
			return (String) selection.getFirstElement();
		}

		return null;
	}

	protected void configurationSelected() {
		selectedConfiguration = getCurrentlySelectedConfiguration();
	}

	protected class ConfigurationProvider implements
			IStructuredContentProvider, ILabelProvider {
		public void dispose() {
			// ignore
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// ignore
		}

		public Object[] getElements(Object inputElement) {
			ArrayList<String> configList = new ArrayList<String>();
			File serverDirectory = new File(inputElement.toString());

			if (serverDirectory.exists()) {

				File types[] = serverDirectory.listFiles();
				for (int i = 0; i < types.length; i++) {
					File serviceDescriptor = new File(types[i]
							.getAbsolutePath()
							+ File.separator
							+ "conf" //$NON-NLS-1$
							+ File.separator
							+ "jboss-service.xml"); //$NON-NLS-1$

					if (types[i].isDirectory() && serviceDescriptor.exists()) {
						String configuration = types[i].getName();
						configList.add(configuration);
					}
				}

				if (configList.size() > 0) {
					getControl().setEnabled(true);
				}
			}

			return configList.toArray();
		}

		public void addListener(ILabelProviderListener listener) {
			// ignore
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
			// ignore
		}

		public Image getImage(Object element) {
			return JBossServerUISharedImages
					.getImage(JBossServerUISharedImages.IMG_JBOSS_CONFIGURATION);
		}

		public String getText(Object element) {
			return (String) element;
		}
	}
}
