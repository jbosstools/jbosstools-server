/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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
