/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.ui.preferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.ui.UIUtil;

public class ServerTypePreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {
	
	public ServerTypePreferencePage() {
	}

	public ServerTypePreferencePage(String title) {
		super(title);
	}

	public ServerTypePreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	public void init(IWorkbench workbench) {
	}

	@Override
	protected Control createContents(Composite parent) {
		ServerTypePreferenceComposite c = new ServerTypePreferenceComposite(parent, SWT.NONE);
		c.setLayoutData(new GridData(GridData.FILL_BOTH));
		c.layout();
		return c;
	}
		
	public static class ServerTypePreferenceComposite extends Composite {
		
		protected ArrayList<String> changed;
		protected HashMap<String, ArrayList<Object>> cacheMap;

		protected Combo servers;
		protected TreeViewer viewer;
		protected Button addButton;
		protected Button removeButton;
		protected Button moveUp;
		protected Button moveDown;
		public ServerTypePreferenceComposite(Composite parent, int style) {
			super(parent, style);
			setLayout(new FormLayout());
			Label l = new Label(this, SWT.None);
			l.setText(getDescriptionLabel());
			l.setLayoutData(createFormData(0,5,null,0,0,5,100,-5));
			
			servers = new Combo(this, SWT.READ_ONLY);
			servers.setLayoutData(createFormData(l,5,null,0,0,5,null,0));
			servers.setItems(getComboItems());
			
			viewer = new TreeViewer(this);
			viewer.getTree().setLayoutData(createFormData(
					servers,5,100,-5,0,5,80,0));
			viewer.setLabelProvider(getLabelProvider());
			viewer.setContentProvider(getContentProvider());
			viewer.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					handleViewerSelectionChanged();
				}
			});
			
			
			Composite buttonWrapper = new Composite(this, SWT.NONE);
			buttonWrapper.setLayoutData(createFormData(servers,5,null,0,viewer.getTree(),5,100,-5));
			buttonWrapper.setLayout(new GridLayout(1, true));
			
			createRightColumnButtons(buttonWrapper);
			servers.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
				}
				public void widgetSelected(SelectionEvent e) {
					getCurrentServerDataModel(); // force load
					viewer.refresh();
					updateMoveUpMoveDown();
				} });
	
			servers.select(0);
			initializeDataModel(); // force load
			viewer.setInput(ResourcesPlugin.getWorkspace());
			updateMoveUpMoveDown();
		}
		
		protected void handleViewerSelectionChanged() {
			updateMoveUpMoveDown();
		}
		protected void updateMoveUpMoveDown() {
			boolean enableUp = true;
			boolean enableDown = true;
			IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
			if( sel != null ) {
				if( sel.size() > 1 || sel.size() == 0 ) {
						enableUp = false;
						enableDown = false;
				}
				if( sel.getFirstElement() == null ) {
					enableUp = enableDown = false;
				} else {
					Object selected = sel.getFirstElement();
					Object[] arr = getCurrentServerDataModel();
					ArrayList<Object> asList = new ArrayList<Object>();
					asList.addAll(Arrays.asList(arr));
					int index = asList.indexOf(selected);
					if( index == 0 ) {
						enableUp = false;
					}
					if( index == arr.length - 1) {
						enableDown = false;
					}
				}
			}
			if( moveUp != null ) 
				moveUp.setEnabled(enableUp);
			if( moveDown != null )
				moveDown.setEnabled(enableDown);
		}
		
		protected void initializeDataModel() {
			cacheMap = new HashMap<String, ArrayList<Object>>();
			changed = new ArrayList<String>();
			getCurrentServerDataModel();
		}
		
		public String getDescriptionLabel() {
			return "Desc label";
		}
		protected void createRightColumnButtons(Composite c) {
			insertAddRemove(c);
			insertMoveUpDown(c);
		}
		
		protected void insertAddRemove(Composite c) {
			addButton = new Button(c, SWT.PUSH);
			addButton.setText("Add...");
			addButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			removeButton = new Button(c, SWT.PUSH);
			removeButton.setText("Remove");
			removeButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			addButton.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					addPressed();
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			
			removeButton.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					removePressed();
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}

		protected void insertMoveUpDown(Composite c) {
			moveUp = new Button(c, SWT.PUSH);
			moveUp.setText("MoveUp");
			moveUp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			moveDown = new Button(c, SWT.PUSH);
			moveDown.setText("Move down");
			moveDown.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			moveUp.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					moveUpPressed();
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			
			moveDown.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					moveDownPressed();
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}
		protected Object[] getCurrentServerDataModel() {
			return new Object[]{};
		}
	
		
		protected void addObject(Object o) {
			if( o != null ) {
				String id = getCurrentId();
				cacheMap.get(id).add(o);
				if( !changed.contains(id))
					changed.add(id);
				viewer.refresh();
			}
		}

		protected void removeObject(Object o) {
			if( o != null ) {
				String id = getCurrentId();
				cacheMap.get(id).remove(o);
				if( !changed.contains(id))
					changed.add(id);
				viewer.refresh();
			}
		}
		
		protected void removePressed() {
			IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
			Object o = sel.getFirstElement();
			removeObject(o);
		}
		protected void addPressed() {
			// TODO
		}
		
		protected void moveUpPressed() {
			IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
			Object fs = (Object)sel.getFirstElement();
			String id = getCurrentId();
			Object[] sets = getCurrentServerDataModel();
			ArrayList<Object> asList = new ArrayList<Object>();
			asList.addAll(Arrays.asList(sets));
			int ind = asList.indexOf(fs);
			asList.remove(ind);
			asList.add(ind-1, fs);
			cacheMap.put(id, asList);
			if( !changed.contains(id))
				changed.add(id);
			updateMoveUpMoveDown();
			viewer.refresh();
		}
	
		protected void moveDownPressed() {
			IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
			Object fs = (Object)sel.getFirstElement();
			String id = getCurrentId();
			Object[] sets = getCurrentServerDataModel();
			ArrayList<Object> asList = new ArrayList<Object>();
			asList.addAll(Arrays.asList(sets));
			int ind = asList.indexOf(fs);
			asList.remove(ind);
			asList.add(ind+1, fs);
			cacheMap.put(id, asList);
			if( !changed.contains(id))
				changed.add(id);
			updateMoveUpMoveDown();
			viewer.refresh();
		}
	
		protected String getCurrentId() {
			int index = servers.getSelectionIndex();
			if( showAllServerTypeOption() && index == 0 )
				return "All Servers";
			if( index == -1 )
				return null;
			int i2 = showAllServerTypeOption() ? index - 1 : index;
			IServerType type = getServerTypes()[i2];
			String id = type.getId();
			return id;
		}
		
		public ArrayList<Object> getDataForServer(String serverId) {
			return cacheMap.get(serverId);
		}
	
		protected LabelProvider getLabelProvider() {
			return new LabelProvider();
		}
		
		protected ITreeContentProvider getContentProvider() {
			return new ITreeContentProvider() {
				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				}
				
				public void dispose() {
				}
				public Object[] getElements(Object inputElement) {
					return getCurrentServerDataModel();
				}
				public boolean hasChildren(Object element) {
					return false;
				}
				public Object getParent(Object element) {
					return null;
				}
				public Object[] getChildren(Object parentElement) {
					return null;
				}
			};
		}
		
		// Return true if all server types, false if only jbt server types
		protected boolean getAllServerTypes() {
			return true;
		}
		// Return true if we show a combo option for "all server types" or not
		protected boolean showAllServerTypeOption() {
			return true;
		}
		

		private String[] getComboItems() {
			ArrayList<String> list = new ArrayList<String>();
			if( showAllServerTypeOption())
				list.add(0,"All Server Types");
			IServerType[] types = getServerTypes();
			for( int i = 0; i < types.length; i++ ) 
				list.add(types[i].getName());
			return (String[]) list.toArray(new String[list.size()]);
		}
		
		private IServerType[] types = null;
		private IServerType[] getServerTypes() {
			if( types == null ) {
				ArrayList<IServerType> retval = new ArrayList<IServerType>();
				ArrayList<IServerType> all = new ArrayList<IServerType>(
						Arrays.asList( ServerCore.getServerTypes()));
				if( !getAllServerTypes()) {
					Iterator<IServerType> i = all.iterator();
					IServerType t;
					while(i.hasNext()) {
						t = i.next();
						if( !t.getId().startsWith("org.jboss.ide.eclipse.as.")) {//$NON-NLS-1$
							i.remove();
						}
					}
				}
				retval = all;
				Collections.sort(retval, new Comparator<IServerType>(){
					public int compare(IServerType o1, IServerType o2) {
						return o1.getName().compareTo(o2.getName());
					}});
				types = (IServerType[]) all.toArray(new IServerType[all.size()]); 
			}
			return types;
		}
		
		private FormData createFormData(Object topStart, int topOffset,
				Object bottomStart, int bottomOffset, Object leftStart,
				int leftOffset, Object rightStart, int rightOffset) {
			return UIUtil.createFormData2(topStart, topOffset, bottomStart, bottomOffset, leftStart, leftOffset, rightStart, rightOffset);
		}
		
		public String[] getChanged() {
			return (String[]) changed.toArray(new String[changed.size()]);
		}
		
		public void clearChanged() {
			changed.clear();
		}
	} // inner class ends

	public boolean performOk() {
    	// TODO do stuff 
        return true;
    }
}
