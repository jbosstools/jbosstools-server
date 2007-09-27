package org.jboss.ide.eclipse.as.ui.views.server.providers.jmx;

import javax.management.MBeanParameterInfo;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXModel.WrappedMBeanOperationInfo;

public class OperationGroup extends Composite {
	protected Tree tree;
	protected TreeColumn nameColumn, typeColumn, 
						valueColumn, descriptionColumn;
	protected TreeViewer treeViewer;
	protected JMXPropertySheetPage page;
	protected Button executeButton;

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
				if( columnIndex == 2 ) return "";
				if( columnIndex == 3 ) return info.getDescription();
			}
			return "";
		}
		
	}
	
	public void setOperation(WrappedMBeanOperationInfo op) {
		treeViewer.setInput(op);
	}

}
