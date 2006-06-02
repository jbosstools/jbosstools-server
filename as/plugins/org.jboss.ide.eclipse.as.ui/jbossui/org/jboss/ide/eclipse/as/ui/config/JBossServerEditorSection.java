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
package org.jboss.ide.eclipse.as.ui.config;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.util.JBossConfigurationTableViewer;

public class JBossServerEditorSection extends ServerEditorSection {

		public JBossServerEditorSection() {
			// do nothing
		}

		private JBossServer jbossServer;
		private Table configurations;
		private JBossConfigurationTableViewer cfgViewer;
		private String selectedConfiguration;
		
		public void init(IEditorSite site, IEditorInput input) {
			super.init(site, input);
			
			if (server != null)
			{
				jbossServer = (JBossServer) server.loadAdapter(JBossServer.class, null);
			}
		}
		
		public void createSection(Composite parent)
		{
			super.createSection(parent);
			
			FormToolkit toolkit = new FormToolkit(parent.getDisplay());
			
			Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE|ExpandableComposite.EXPANDED|ExpandableComposite.TITLE_BAR|Section.DESCRIPTION);
			section.setText(JBossServerUIPlugin.getResourceString("%serverEditorGeneralSection"));
			section.setDescription(JBossServerUIPlugin.getResourceString("%serverEditorGeneralDescription"));
			section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
			
			Composite composite = toolkit.createComposite(section);
			GridLayout layout = new GridLayout();
			layout.numColumns = 3;
			layout.marginHeight = 5;
			layout.marginWidth = 10;
			layout.verticalSpacing = 5;
			layout.horizontalSpacing = 15;
			composite.setLayout(layout);
			composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
			toolkit.paintBordersFor(composite);
			section.setClient(composite);
			
			Label label = toolkit.createLabel(composite, JBossServerUIPlugin.getResourceString("%serverEditorConfiguration"));
			configurations = toolkit.createTable(composite, SWT.BORDER | SWT.SINGLE);
			cfgViewer = new JBossConfigurationTableViewer(configurations);
			GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
			data.horizontalSpan = 3;
			configurations.setLayoutData(data);
			cfgViewer.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent se) {
					configurationSelected();
				}
			});
			
//			IJBossConfiguration config = jbossServer.getConfiguration();
//			cfgViewer.setJBossHome(config.getJBossServerHome());
//			cfgViewer.setDefaultConfiguration(config.getJBossConfigurationName());
		}
		
		protected String getSelectedConfiguration ()
		{
			if (cfgViewer.getSelection() instanceof IStructuredSelection)
			{
				IStructuredSelection selection = (IStructuredSelection) cfgViewer.getSelection();
				return (String) selection.getFirstElement();	
			}
			
			return null;
		}
		
		protected void configurationSelected ()
		{
//			IJBossConfiguration config = jbossServer.getConfiguration();
//			config.setJBossConfigurationName(cfgViewer.getSelectedConfiguration());			
//			config.save();
		}

		public void dispose() {
			// ignore
		}
}
