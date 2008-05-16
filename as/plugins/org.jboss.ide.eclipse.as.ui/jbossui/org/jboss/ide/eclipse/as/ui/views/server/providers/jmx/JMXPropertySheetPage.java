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

import java.util.ArrayList;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXModel.JMXAttributesWrapper;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXModel.JMXBean;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXModel.JMXDomain;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXModel.WrappedMBeanOperationInfo;
import org.jboss.ide.eclipse.as.ui.views.server.providers.jmx.JMXViewProvider.ErrorGroup;
import org.jboss.tools.wst.server.ui.views.server.ExtensionTableViewer.ContentWrapper;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class JMXPropertySheetPage implements IPropertySheetPage {

	// data
	protected JMXViewProvider provider;
	protected JMXBean bean;
	protected WrappedMBeanOperationInfo[] operations;

	// ui pieces
	protected Composite main, domainGroup;
	protected Combo pulldown;
	protected Label beanLabel;
	protected PageBook book;
	protected ErrorGroup errorGroup;
	protected OperationGroup operationGroup;
	protected AttributeGroup attributeGroup;

	public JMXPropertySheetPage(JMXViewProvider provider) {
		this.provider = provider;
	}

	public void createControl(Composite parent) {
		main = new Composite(parent, SWT.NONE);
		main.setLayout(new FormLayout());

		pulldown = new Combo(main, SWT.READ_ONLY);
		FormData pulldownData = new FormData();
		pulldownData.left = new FormAttachment(0, 10);
		pulldownData.right = new FormAttachment(0,400);
		pulldownData.top = new FormAttachment(0, 10);
		pulldown.setLayoutData(pulldownData);

		beanLabel = new Label(main, SWT.NONE);
		FormData beanLabelData = new FormData();
		beanLabelData.left = new FormAttachment(0, 10);
		beanLabelData.top = new FormAttachment(pulldown, 3);
		beanLabel.setLayoutData(beanLabelData);

		// small font size
		Font initialFont = beanLabel.getFont();
		FontData[] fontData = initialFont.getFontData();
		for (int i = 0; i < fontData.length; i++) {
			fontData[i].setHeight(7);
		}
		Font newFont = new Font(Display.getDefault(), fontData);
		beanLabel.setFont(newFont);
		
		Listener listener = new Listener() {
			public void handleEvent(Event event) {
				switch (event.type) {
				case SWT.Selection:
				case SWT.Modify:
					pulldownSelectionChanged();
					break;
				}
			}
		};
		pulldown.addListener(SWT.Selection, listener);
		pulldown.addListener(SWT.Modify, listener);

		book = new PageBook(main, SWT.NONE);
		FormData bookData = new FormData();
		bookData.top = new FormAttachment(beanLabel, 5);
		bookData.left = new FormAttachment(0, 5);
		bookData.right = new FormAttachment(100, -5);
		bookData.bottom = new FormAttachment(100, -5);
		book.setLayoutData(bookData);

		errorGroup = new ErrorGroup(book, SWT.NONE);
		operationGroup = new OperationGroup(book, SWT.NONE, this);
		attributeGroup = new AttributeGroup(book, SWT.NONE, this);
		domainGroup = new Composite(book, SWT.NONE);
	}

	public void dispose() {
	}

	public Control getControl() {
		return main;
	}

	public void setActionBars(IActionBars actionBars) {
	}

	public void setFocus() {
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			Object obj = ((IStructuredSelection) selection).getFirstElement();
			if (obj instanceof ContentWrapper)
				obj = ((ContentWrapper) obj).getElement();

			if (obj == null)
				return;
			setInputObject(obj);
		}
	}

	protected void setInputObject(Object obj) {
		if (obj instanceof JMXDomain) {
			showDomainComposite((JMXDomain) obj);
			setBean(null);
		} else if (obj instanceof JMXBean) {
			setBean((JMXBean) obj);
		} else if (obj instanceof JMXAttributesWrapper
				|| obj instanceof WrappedMBeanOperationInfo) {
			setBean(getBeanFromInput(obj));
			setComboSelectionFromInput(obj);
		}
	}

	protected void setBean(JMXBean bean) {
		if (bean != null) {
			boolean requiresLoading = bean.getOperations() == null
					&& bean.getException() == null;
			boolean hasError = bean.getOperations() == null
					&& bean.getException() != null;
			boolean finishedLoading = bean.getOperations() != null;

			this.bean = bean;
			beanLabel.setText("for MBean " + bean.getName());
			if (requiresLoading) {
				pulldown.setItems(JMXViewProvider.LOADING_STRING_ARRAY);
				pulldown.select(0); // select Loading...
				loadChildrenRefreshProperties(bean);
				return; // do not re-layout during loading 
			} else if (hasError) {
				// some error
				showErrorComposite();
			} else if (finishedLoading) {
				// finished loading
				operations = bean.getOperations();
				pulldown.setItems(pulldownItems());
				pulldown.select(0); // select Loading...
				beanLabel.setText("for MBean " + bean.getName() + "  ("
						+ operations.length + " operations, "
						+ bean.getAttributes().length + " attributes)");
			}
			main.layout();
		} else {
			beanLabel.setText("Please select an mbean from the JBoss Servers View");
			pulldown.setItems(JMXViewProvider.SELECT_MBEAN_ARRAY);
			pulldown.select(0); // select Loading...
		}
	}

	protected JMXBean getBeanFromInput(Object obj) {
		return obj instanceof JMXAttributesWrapper ? ((JMXAttributesWrapper) obj)
				.getBean() : ((WrappedMBeanOperationInfo) obj).getBean();
	}

	protected void setComboSelectionFromInput(Object obj) {
		int index = -1;
		if (obj instanceof WrappedMBeanOperationInfo)
			index = pulldown
					.indexOf(getStringForOperation(((WrappedMBeanOperationInfo) obj)));
		if (index == -1)
			index = 0;
		pulldown.select(index);
	}

	// get the list of combo items based on the bean
	// The list should be Two larger than the number of operations
	// ex: {Attributes..., ------, op1, op2, op3... etc}
	protected String[] pulldownItems() {
		WrappedMBeanOperationInfo[] ops = bean.getOperations();
		String[] vals = null;
		if (ops != null) {
			if (ops.length == 0)
				return new String[] { JMXViewProvider.ATTRIBUTES_STRING };
			vals = new String[ops.length + 2];
			vals[0] = JMXViewProvider.ATTRIBUTES_STRING;
			vals[1] = "---------------";
			for (int i = 0; i < ops.length; i++) {
				vals[i + 2] = getStringForOperation(ops[i]);
			}
		}
		return vals;
	}

	protected String getStringForOperation(WrappedMBeanOperationInfo op) {
		return op.getInfo().getReturnType() + " " + op.getInfo().getName();
	}

	public void loadChildrenRefreshProperties(final Object bean) {
		new Thread() {
			public void run() {
				provider.loadChildren(bean);
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						setInputObject(bean);
					}
				});
			}
		}.start();
	}

	protected void showDomainComposite(JMXDomain domain) {
		book.showPage(domainGroup);
	}

	protected void showErrorComposite() {
		book.showPage(errorGroup);
	}

	protected void pulldownSelectionChanged() {
		if (pulldown.getSelectionIndex() != -1) {
			int index = pulldown.getSelectionIndex();
			if (index == 0 && pulldown.getItem(0).equals(JMXViewProvider.ATTRIBUTES_STRING)) {
				attributeGroup.setBean(bean);
				book.showPage(attributeGroup);
			} else if (index >= 2 && operations != null
					&& index <= operations.length + 1) {
				String selected = pulldown
						.getItem(pulldown.getSelectionIndex());
				book.showPage(operationGroup);
				operationGroup.setOperation(findOperation(bean, selected));
			}
		}
	}
	protected WrappedMBeanOperationInfo findOperation(JMXBean bean, String selected) {
		WrappedMBeanOperationInfo[] opInfos = bean.getOperations();
		for( int i = 0; i < opInfos.length; i++ ) {
			if( getStringForOperation(opInfos[i]).equals(selected))
				return opInfos[i];
		}
		return null;
	}

	
	public static Object box(String type, String text) {
		Object val = null;
		try {
			if( type.equals("java.lang.String")) val = text;
			else if( type.equals("boolean")) val = new Boolean(text);
			else if( type.equals("int")) val = new Integer(text);
			else if( type.equals("long")) val = new Long(text);
		} catch( Exception e ) {}
		return val;
	}
	
	protected static ArrayList<String> simpleTypeList = null;
	public static boolean isSimpleType(String fullClassName) {
		if( simpleTypeList == null ) {
			simpleTypeList = new ArrayList<String>();
			simpleTypeList.add("java.lang.String");
			simpleTypeList.add("boolean");
			simpleTypeList.add("int");
			simpleTypeList.add("long");
		}
		return simpleTypeList.contains(fullClassName);
	}

}
