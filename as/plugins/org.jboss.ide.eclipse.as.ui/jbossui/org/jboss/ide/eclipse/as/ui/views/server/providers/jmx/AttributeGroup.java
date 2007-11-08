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

import javax.management.Attribute;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXModel.JMXBean;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXModel.JMXRunnable;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXModel.JMXSafeRunner;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXModel.WrappedMBeanAttributeInfo;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.views.server.providers.jmx.TreeEditorSelectionListener.SelectionCallbackHandler;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class AttributeGroup extends Composite {

	protected Tree tree;
	protected TreeColumn nameColumn, typeColumn, accessColumn, valueColumn,
			descriptionColumn;
	protected TreeViewer treeViewer;
	protected JMXPropertySheetPage page;

	public AttributeGroup(Composite parent, int style, JMXPropertySheetPage page) {
		super(parent, style);
		this.page = page;
		setLayout(new FillLayout());

		tree = new Tree(this, SWT.BORDER | SWT.FULL_SELECTION);

		nameColumn = new TreeColumn(tree, SWT.NONE);
		typeColumn = new TreeColumn(tree, SWT.NONE);
		accessColumn = new TreeColumn(tree, SWT.NONE);
		valueColumn = new TreeColumn(tree, SWT.NONE);
		descriptionColumn = new TreeColumn(tree, SWT.NONE);

		nameColumn.setWidth(100);
		typeColumn.setWidth(150);
		accessColumn.setWidth(50);
		valueColumn.setWidth(200);
		descriptionColumn.setWidth(300);

		nameColumn.setText("Name");
		typeColumn.setText("Type");
		accessColumn.setText("Access");
		valueColumn.setText("Value");
		descriptionColumn.setText("Description");

		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		treeViewer = new TreeViewer(tree);
		treeViewer.setContentProvider(new AttributeViewerContentProvider());
		treeViewer.setLabelProvider(new AttributeViewerLabelProvider());

		SelectionCallbackHandler callback = new SelectionCallbackHandler() {
			public WrappedMBeanAttributeInfo getWrapped(TreeItem item) {
				if (!(item.getData() instanceof WrappedMBeanAttributeInfo))
					return null;
				return (WrappedMBeanAttributeInfo) item.getData();
			}
			public boolean canModify(TreeItem item) {
				WrappedMBeanAttributeInfo info = getWrapped(item);
				if (!info.getInfo().isWritable()) return false;
				if( !JMXPropertySheetPage.isSimpleType(info.getInfo().getType())) return false;
				return true;
			}
			public void cannotModify(TreeItem item) {
				WrappedMBeanAttributeInfo info = getWrapped(item);
				if (!info.getInfo().isWritable()) return;
				if( !JMXPropertySheetPage.isSimpleType(info.getInfo().getType())) return;
					handleComplexType(info);
			}
			public void handleChange(TreeItem item, Text text) {
				saveAttributeChange(getWrapped(item), text);
			}
		};
		TreeEditorSelectionListener selListener = new TreeEditorSelectionListener(tree, 3, callback);
		tree.addListener(SWT.MouseDoubleClick, selListener);
	}

	public void setBean(JMXBean bean) {
		treeViewer.setInput(bean);
	}

	protected void saveAttributeChange(WrappedMBeanAttributeInfo attInfo, Text text) {
		if (!text.isDisposed()) {
			Attribute att = createAttribute(attInfo, text.getText());
			if( att == null ) {
				// throw up a message box and say no can do, for now
				MessageBox messageBox = new MessageBox (new Shell(), SWT.OK);
				messageBox.setText ("Cannot update bean");
				messageBox.setMessage ("Bean update cannot proceed. Plug-in cannot convert " + text.getText() + " into " + attInfo.getInfo().getType());
				messageBox.open();
				return;
			}
			saveAttributeChange(attInfo, att);
		}
	}
	
	protected void saveAttributeChange(final WrappedMBeanAttributeInfo attInfo, final Attribute att) {
		if( att == null ) {
			// throw up a message box and say no can do, for now
			MessageBox messageBox = new MessageBox (new Shell(), SWT.OK);
			messageBox.setText ("Cannot update bean");
			messageBox.setMessage ("Bean update cannot proceed. Plug-in cannot convert " + att.getValue().toString() + " into " + attInfo.getInfo().getType());
			messageBox.open();
			return;
		}
		final Boolean[] errorBool = new Boolean[1];
		final JMXRunnable run = new JMXRunnable() {
			public void run(MBeanServerConnection connection) {
				try {
					connection.setAttribute(new ObjectName(
							attInfo.getBean().getName()), att);
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
			JMXSafeRunner.run(attInfo.getBean().getServer(), run);
			if( errorBool[0] == null ) {
				page.provider.loadChildren(attInfo.getBean());
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						page.setInputObject(attInfo.getBean());
					}
				});
			}
		}}.start();
	}

	protected Attribute createAttribute(WrappedMBeanAttributeInfo attInfo, String text) {
		String type = attInfo.getInfo().getType();
		Object val = null;
		if( type != null ) {
			val = JMXPropertySheetPage.box(type, text);
		}
		return val == null ? null :  
			new Attribute(attInfo.getInfo().getName(), val);
	}
		
	protected void handleComplexType(WrappedMBeanAttributeInfo info) {
		// throw up a message box and say no can do, for now
		MessageBox messageBox = new MessageBox (new Shell(), SWT.OK);
		messageBox.setText ("Cannot update bean");
		messageBox.setMessage ("Bean update cannot proceed. Plug-in cannot create instances of " + info.getInfo().getType() + " at this time.");
		messageBox.open();
		return;
	}
	
	
	protected class AttributeViewerContentProvider implements
			ITreeContentProvider {
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof JMXBean)
				return ((JMXBean) parentElement).getAttributes();
			return new Object[] {};
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return false;
		}

		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof JMXBean)
				return ((JMXBean) inputElement).getAttributes();
			return new Object[] {};
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	protected class AttributeViewerLabelProvider extends LabelProvider
			implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof WrappedMBeanAttributeInfo) {
				WrappedMBeanAttributeInfo element2 = (WrappedMBeanAttributeInfo) element;
				if (columnIndex == 0)
					return element2.getInfo().getName();
				if (columnIndex == 1)
					return element2.getInfo().getType();
				if (columnIndex == 3)
					return (element2.getValue() != null ? element2.getValue()
							.toString() : "");
				if (columnIndex == 4)
					return element2.getInfo().getDescription();
				if (columnIndex == 2) {
					String ret = element2.getInfo().isReadable() ? "R" : "";
					ret += element2.getInfo().isWritable() ? "W" : "";
					return ret;
				}
			}
			return null;
		}

	}

}
