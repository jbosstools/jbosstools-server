/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
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
package org.jboss.ide.eclipse.as.ui.views.server.providers.jmx;

import java.util.HashMap;

import javax.management.MBeanParameterInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXModel.JMXRunnable;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXModel.JMXSafeRunner;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXModel.WrappedMBeanOperationInfo;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXModel.WrappedMBeanOperationParameter;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class OperationGroup extends Composite {
	protected Tree tree;
	protected TreeColumn nameColumn, typeColumn, 
						valueColumn, descriptionColumn;
	protected TreeViewer treeViewer;
	protected JMXPropertySheetPage page;
	protected Button executeButton;
	protected WrappedMBeanOperationInfo selectedOperation;

	public OperationGroup(Composite parent, int style, JMXPropertySheetPage page) {
		super(parent, style);
		this.page = page;
		setLayout(new FormLayout());
		
		executeButton = new Button(this, SWT.PUSH);
		executeButton.setText("execute");
		FormData executeData = new FormData();
		executeData.right = new FormAttachment(100,0);
		executeData.top = new FormAttachment(0,0);
		executeData.bottom = new FormAttachment(100,0);
		executeButton.setLayoutData(executeData);
		
		
		tree = new Tree(this, SWT.BORDER | SWT.FULL_SELECTION);
		FormData treeData = new FormData();
		treeData.left = new FormAttachment(0,0);
		treeData.right = new FormAttachment(executeButton, 0);
		treeData.top = new FormAttachment(0,0);
		treeData.bottom = new FormAttachment(100,0);
		tree.setLayoutData(treeData);
		
		
		nameColumn = new TreeColumn(tree, SWT.NONE);
		typeColumn = new TreeColumn(tree, SWT.NONE);
		valueColumn = new TreeColumn(tree, SWT.NONE);
		descriptionColumn = new TreeColumn(tree, SWT.NONE);

		nameColumn.setWidth(100);
		typeColumn.setWidth(150);
		valueColumn.setWidth(200);
		descriptionColumn.setWidth(300);
		
		nameColumn.setText("Name");
		typeColumn.setText("Type");
		valueColumn.setText("Value");
		descriptionColumn.setText("Description");

		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		treeViewer = new TreeViewer(tree);
		treeViewer.setContentProvider(new OperationViewerContentProvider());
		treeViewer.setLabelProvider(new OperationViewerLabelProvider());

//		JMXAttributePropertySelListener selListener = new JMXOperationPropertySelListener();
//		tree.addListener(SWT.MouseDoubleClick, selListener);

		executeButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
			public void widgetSelected(SelectionEvent e) {
				executePressed();
			} 
		});
	}
	
	protected void executePressed() {
		final Boolean[] errorBool = new Boolean[1];
		final JMXRunnable run = new JMXRunnable() {
			public void run(MBeanServerConnection connection) {
				try {
					ObjectName on = selectedOperation.getBean().getObjectName();
					String opName = selectedOperation.getInfo().getName();
					connection.invoke(on, opName, getParams(), getSignatures());
				} catch (final Exception e) {
					Display.getDefault().asyncExec(new Runnable() { public void run() {
						IStatus status = new Status(IStatus.ERROR, JBossServerUIPlugin.PLUGIN_ID, e.getMessage(), e);
						JBossServerUIPlugin.getDefault().getLog().log(status);
						errorBool[0] = new Boolean(true);
					}});
				}
			}
		};
		
		new Thread() { public void run() { 
			JMXSafeRunner.run(selectedOperation.getBean().getServer(), run);
			if( errorBool[0] == null ) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						// display error
					}
				});
			}
		}}.start();

	}
	
	protected class OperationViewerContentProvider implements
		ITreeContentProvider {

		public Object[] getChildren(Object parentElement) {
			if( parentElement != null && parentElement instanceof WrappedMBeanOperationInfo ) {
				return ((WrappedMBeanOperationInfo)parentElement).getInfo().getSignature();
			}
			return new Object[] {};
		}
		public Object getParent(Object element) {
			return null;
		}
		public boolean hasChildren(Object element) {
			return false;
		}
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		
	}

		
	
	protected class OperationViewerLabelProvider extends LabelProvider
		implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if( element instanceof MBeanParameterInfo ) {
				MBeanParameterInfo info = (MBeanParameterInfo)element;
				if( columnIndex == 0 ) return info.getName();
				if( columnIndex == 1 ) return info.getType();
				if( columnIndex == 2 ) return opParams.get(element) == null ? "null" : opParams.get(element).toString();
				if( columnIndex == 3 ) return info.getDescription();
			}
			return "";
		}
		
	}
	
	protected HashMap<MBeanParameterInfo, Object> opParams;
	public void setOperation(WrappedMBeanOperationInfo op) {
		opParams = new HashMap<MBeanParameterInfo, Object>();
		WrappedMBeanOperationParameter[] params = op.getParameters();
		for( int i = 0; i < params.length; i++ ) {
			opParams.put(params[i].getInfo(), null);
		}
		selectedOperation = op;
		treeViewer.setInput(op);
	}

	protected Object[] getParams() {
		WrappedMBeanOperationParameter[] params = selectedOperation.getParameters();
		Object[] paramVals = new Object[params.length];
		for( int i = 0; i < paramVals.length; i++ ) {
			paramVals[i] = opParams.get(params[i].getInfo());
		}
		return paramVals;
	}
	
	protected String[] getSignatures() {
		WrappedMBeanOperationParameter[] params = selectedOperation.getParameters();
		String[] signatures = new String[selectedOperation.getParameters().length];
		for( int i = 0; i < signatures.length; i++ ) {
			signatures[i] = params[i].getInfo().getType();
		}
		return signatures;
	}
}
