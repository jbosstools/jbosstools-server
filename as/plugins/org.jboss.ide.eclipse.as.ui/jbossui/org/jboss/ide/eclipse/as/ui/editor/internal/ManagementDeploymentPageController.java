/******************************************************************************* 
* Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.ui.editor.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.ServerEvent;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.editor.DeploymentPage;
import org.jboss.ide.eclipse.as.ui.editor.IDeploymentPageUIController;
import org.jboss.ide.eclipse.as.ui.editor.ModuleDeploymentOptionsComposite;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;
import org.jboss.tools.as.core.internal.modules.DeploymentModulePrefs;
import org.jboss.tools.as.core.internal.modules.DeploymentPreferences;

/**
 * The deployment page for showing deployment options 
 * for servers using management deployment
 */
public class ManagementDeploymentPageController extends
		AbstractSubsystemController implements IDeploymentPageUIController {

	protected DeploymentPage page;
	protected ModuleDeploymentOptionsComposite perModuleOptions;
	
	public DeploymentPage getPage() {
		return page;
	}
	
	@Override
	public void serverChanged(ServerEvent event) {
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input, DeploymentPage page) {
		this.page = page;
	}
	
	
	public void createPartControl(Composite parent) {
		try {
			ScrolledForm innerContent = createPageStructure(parent);
			addDeploymentLocationControls(innerContent.getBody());
			innerContent.reflow(true);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private ScrolledForm createPageStructure(Composite parent) {
		FormToolkit toolkit = getFormToolkit(parent);
		ScrolledForm allContent = toolkit.createScrolledForm(parent);
		toolkit.decorateFormHeading(allContent.getForm());
		allContent.setText(Messages.EditorDeployment);
		allContent.getBody().setLayout(new FormLayout());
		return allContent;
	}

	public FormToolkit getFormToolkit(Composite parent) {
		return page.getFormToolkit(parent);
	}

	@Override
	public void dispose() {
	}
	
	
	protected void addDeploymentLocationControls(Composite parent) {
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		Label l1 = toolkit.createLabel(parent, Messages.EditorDeploymentPageWarning); 
		FormData fd = new FormData();
		fd.left = new FormAttachment(0, 5);
		fd.top = new FormAttachment(0, 5); 
		fd.right = new FormAttachment(100, -5);
		l1.setLayoutData(fd);
		
		// Simply create a composite to show the per-module customizations
		perModuleOptions = new ManagementModuleDeploymentOptionsComposite(parent, getPage(), getFormToolkit(parent), getPage().getPreferences());
		fd = new FormData();
		fd.left = new FormAttachment(0, 5);
		fd.top = new FormAttachment(l1, 5);
		fd.right = new FormAttachment(100, -5);
		fd.bottom = new FormAttachment(100, -5);
		perModuleOptions.setLayoutData(fd);

	}
	
	private static class ManagementModuleDeploymentOptionsComposite extends ModuleDeploymentOptionsComposite {

		public ManagementModuleDeploymentOptionsComposite(Composite parent,
				DeploymentPage partner, FormToolkit tk,
				DeploymentPreferences prefs) {
			super(parent, partner, tk, prefs);
		}
		
		protected TreeViewer createTreeViewer(Composite root) {
			TreeViewer viewer = new TreeViewer(root, SWT.BORDER);
			viewer.getTree().setHeaderVisible(true);
			viewer.getTree().setLinesVisible(true);
			viewer.setContentProvider(createViewerContentProvider());
			viewer.setLabelProvider(createViewerLabelProvider());
			
			TreeColumn moduleColumn = new TreeColumn(viewer.getTree(), SWT.NONE);
			TreeColumn publishNameColumn = new TreeColumn(viewer.getTree(), SWT.NONE);
			moduleColumn.setText(Messages.EditorModule);
			publishNameColumn.setText("Deployment Name"); //$NON-NLS-1$

			moduleColumn.setWidth(200);
			publishNameColumn.setWidth(200);

			viewer.setColumnProperties(new String[] { 
					COLUMN_NAME, COLUMN_LOC});
			viewer.setInput("");  //$NON-NLS-1$
			CellEditor[] editors = new CellEditor[] {
					new TextCellEditor(viewer.getTree()),
					new TextCellEditor(viewer.getTree())};
			editors[1].setValidator(new ICellEditorValidator(){
				public String isValid(Object value) {
					if( ((String)value).contains("/") || ((String)value).contains("\\")) //$NON-NLS-1$ //$NON-NLS-2$
						return "WRONG"; //$NON-NLS-1$
					return null;
				}});
			viewer.setCellEditors(editors);
			viewer.setCellModifier(createViewerCellModifier());
			return viewer;
		}
		
		protected ICellModifier createViewerCellModifier() {
			ModuleDeploymentCellModifier t = new ModuleDeploymentCellModifier(this){
				public Object getValue(Object element, String property) {
					DeploymentModulePrefs p = getPreferences().getOrCreatePreferences()
							.getOrCreateModulePrefs((IModule) element);
					if (property == COLUMN_LOC) {
						String outputName = p.getProperty(OUTPUT_NAME);
						outputName = outputName == null || outputName.length() == 0
							? getDefaultOutputName((IModule)element) : outputName;
						return outputName;
					}
					return ""; //$NON-NLS-1$
				}

				public void modify(Object element, String property, Object value) {
					IModule module = (IModule) ((TreeItem) element).getData();
					DeploymentModulePrefs p = getPreferences().getOrCreatePreferences()
							.getOrCreateModulePrefs(module);
					if (property == COLUMN_LOC) {
						String outputName;
						if( value == null || ((String)value).equals("")) { //$NON-NLS-1$
							outputName = ""; //$NON-NLS-1$
						} else {
							outputName = new Path(((String)value)).lastSegment();
						}
						getComposite().firePropertyChangeCommand(p, 
								new String[]{OUTPUT_NAME},
								new String[]{outputName},
								Messages.EditorEditDeployLocCommand);
						getViewer().refresh();
					} 
				}				
			};
			return t;
		}
	}
	
}
