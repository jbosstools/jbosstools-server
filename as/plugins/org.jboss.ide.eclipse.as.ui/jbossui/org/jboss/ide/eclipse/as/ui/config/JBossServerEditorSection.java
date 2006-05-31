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
