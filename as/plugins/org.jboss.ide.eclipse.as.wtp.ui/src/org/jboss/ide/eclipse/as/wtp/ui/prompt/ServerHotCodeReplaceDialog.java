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
package org.jboss.ide.eclipse.as.wtp.ui.prompt;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.ServerUICore;
import org.jboss.ide.eclipse.as.wtp.core.server.launch.ServerHotCodeReplaceListener;
import org.jboss.ide.eclipse.as.wtp.ui.Messages;
import org.jboss.ide.eclipse.as.wtp.ui.util.FormDataUtility;

public class ServerHotCodeReplaceDialog extends TitleAreaDialog  {
	
	public static final int OBSOLETE_METHODS = ServerHotCodeReplaceListener.EVENT_HCR_OBSOLETE;
	public static final int HCR_FAILED = ServerHotCodeReplaceListener.EVENT_HCR_FAIL;
	
	
	protected IServer server;
	protected int type;
	protected Exception e;
	protected boolean remember;
	
	public ServerHotCodeReplaceDialog(IServer server, int type, Exception e) {
		super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		this.server = server;
		this.type = type;
		this.e = e;
	}
	
	@Override
	protected Control createContents(Composite parent) {
		Control c = super.createContents(parent);
		String shellTitle = type == OBSOLETE_METHODS ? Messages.HotCodeReplaceObsolete_Title : Messages.HotCodeReplaceFailed_Title;
		getShell().setText(shellTitle);
		
		String title = NLS.bind(Messages.HotCodeReplaceHeader, server.getName());
		setTitle(title);
		setMessage(e == null ? "" : e.getMessage(), IMessageProvider.WARNING );
		Shell parentShell = getParentShell();
		Point p2 = parentShell.getLocation();
		getShell().setSize(525,400);
		getShell().setLocation(p2.x + 100, p2.y + 100);
		
		return c;
	}
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, ServerHotCodeReplaceListener.RESTART_MODULE, Messages.hcrRestartModules, false);
		createButton(parent, ServerHotCodeReplaceListener.TERMINATE, Messages.hcrTerminate, false);
		createButton(parent, ServerHotCodeReplaceListener.RESTART_SERVER, Messages.hcrRestartServer, false);
		createButton(parent, ServerHotCodeReplaceListener.CONTINUE, Messages.hcrContinue, false);
	}
	
	protected void buttonPressed(int buttonId) {
		setReturnCode(buttonId);
		close();
	}
	
	protected Control createDialogArea(Composite parent) {
		Composite main = new Composite((Composite)super.createDialogArea(parent), SWT.NONE);
		main.setLayoutData(new GridData(GridData.FILL_BOTH));
		main.setLayout(new FormLayout());

		Label desc = new Label(main, SWT.WRAP);
		desc.setText(NLS.bind(Messages.HotCodeReplaceDesc, server.getName()));
		final IModule[] all = server.getModules();
		
		Composite wrapper = new Composite(main, SWT.NONE);
		//wrapper.setEnabled(false);
		wrapper.setLayout(new FillLayout());
		final TreeViewer tv = new TreeViewer(wrapper, SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		tv.setContentProvider(new ITreeContentProvider() {
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			
			public void dispose() {
			}
			
			public boolean hasChildren(Object element) {
				return false;
			}
			
			public Object getParent(Object element) {
				return null;
			}
			
			public Object[] getElements(Object inputElement) {
				return all;
			}
			
			public Object[] getChildren(Object parentElement) {
				return null;
			}
		});
		tv.addSelectionChangedListener(new ISelectionChangedListener() {
		    public void selectionChanged(final SelectionChangedEvent event) {
		        if (!event.getSelection().isEmpty()) {
		             tv.setSelection(StructuredSelection.EMPTY);
		        }
		    }
		});
		tv.setLabelProvider(ServerUICore.getLabelProvider());
		tv.setInput(all);
		tv.getTree().deselectAll();
		final Button b = new Button(main, SWT.CHECK);
		b.setText(Messages.RememberChoiceServer);
		
		desc.setLayoutData(FormDataUtility.createFormData2(0, 5, null, 0, 0, 5, 100, -5));
		wrapper.setLayoutData(FormDataUtility.createFormData2(desc, 5, b, -5, 0, 5, 100, -5));
		b.setLayoutData(FormDataUtility.createFormData2(null, 0, 100, -5, 0, 5, null, 0));
		b.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				remember = b.getSelection();
			}});
		return main;
	}

	public boolean isHelpAvailable() {
		return false;
	}
	
	public boolean getSaveSetting() {
		return remember;
	}
	
	protected void handleShellCloseEvent() {
		setReturnCode(ServerHotCodeReplaceListener.CONTINUE);
		close();
	}
}
