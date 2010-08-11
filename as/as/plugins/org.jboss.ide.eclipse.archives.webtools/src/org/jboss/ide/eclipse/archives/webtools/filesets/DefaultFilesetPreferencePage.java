package org.jboss.ide.eclipse.archives.webtools.filesets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.archives.webtools.Messages;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;

public class DefaultFilesetPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private Combo servers;
	private TreeViewer viewer;
	private Button addButton;
	private Button removeButton;
	private HashMap<String, ArrayList<Fileset>> cacheMap 
		= new HashMap<String, ArrayList<Fileset>>();
	private ArrayList<String> changed = new ArrayList<String>();
	
	public DefaultFilesetPreferencePage() {
	}

	public DefaultFilesetPreferencePage(String title) {
		super(title);
	}

	public DefaultFilesetPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	public void init(IWorkbench workbench) {
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new FormLayout());
		c.setLayoutData(new GridData(GridData.FILL_BOTH));
		Label l = new Label(c, SWT.None);
		l.setText(Messages.DefaultFilesetsLabel);
		l.setLayoutData(createFormData(0,5,null,0,0,5,100,-5));
		
		servers = new Combo(c, SWT.READ_ONLY);
		servers.setLayoutData(createFormData(l,5,null,0,0,5,null,0));
		servers.setItems(getComboItems());
		
		viewer = new TreeViewer(c);
		viewer.getTree().setLayoutData(createFormData(
				servers,5,100,-5,0,5,80,0));
		viewer.setLabelProvider(getLabelProvider());
		viewer.setContentProvider(getContentProvider());
		
		addButton = new Button(c, SWT.PUSH);
		addButton.setText(Messages.DefaultFilesetsAdd);
		addButton.setLayoutData(createFormData(
				servers,5,null,0,viewer.getTree(),5,100,-5));
		removeButton = new Button(c, SWT.PUSH);
		removeButton.setText(Messages.DefaultFilesetsRemove);
		removeButton.setLayoutData(createFormData(
				addButton,5,null,0,viewer.getTree(),5,100,-5));
		
		servers.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				getCurrentServerSets();
				viewer.refresh();
			} });
		
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

		servers.select(0);
		getCurrentServerSets();
		viewer.setInput(ResourcesPlugin.getWorkspace());
		c.layout();
		return c;
	}

	protected void addPressed() {
		FilesetDialog d = new FilesetDialog(addButton.getShell(), "", null); //$NON-NLS-1$
		d.setShowViewer(false);
		if( d.open() == Window.OK) {
			Fileset fs = d.getFileset();
			String id = getCurrentId();
			cacheMap.get(id).add(fs);
			if( !changed.contains(id))
				changed.add(id);
			viewer.refresh();
		}
	}

	protected void removePressed() {
		Fileset fs = null;
		IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
		fs = (Fileset)sel.getFirstElement();
		if( fs != null ) {
			String id = getCurrentId();
			cacheMap.get(id).remove(fs);
			if( !changed.contains(id))
				changed.add(id);
			viewer.refresh();
		}
	}

	protected String getCurrentId() {
		int index = servers.getSelectionIndex();
		if( index == 0 )
			return FilesetUtil.DEFAULT_FS_ALL_SERVERS;
		if( index == -1 )
			return null;
		IServerType type = getServerTypes()[index-1];
		String id = type.getId();
		return id;
	}
	protected Fileset[] getCurrentServerSets() {
		String id = getCurrentId();
		List<Fileset> list = new ArrayList<Fileset>();
		if( id != null ) {
			list = cacheMap.get(id);
			if( list == null ) {
				IPath fileToRead = FilesetUtil.DEFAULT_FS_ROOT.append(id);
				Fileset[] sets = FilesetUtil.loadFilesets(fileToRead.toFile(), null);
				list = Arrays.asList(sets);
				cacheMap.put(id, new ArrayList<Fileset>(list));
			}
		}
		return (Fileset[]) list.toArray(new Fileset[list.size()]);
	}
	
	protected LabelProvider getLabelProvider() {
		return new FilesetLabelProvider();
	}
	
	protected ITreeContentProvider getContentProvider() {
		return new ITreeContentProvider() {
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			
			public void dispose() {
			}
			public Object[] getElements(Object inputElement) {
				return getCurrentServerSets();
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
	
	private String[] getComboItems() {
		ArrayList<String> list = new ArrayList<String>();
		IServerType[] types = getServerTypes();
		list.add(Messages.DefaultFilesetsAllServerTypes);
		for( int i = 0; i < types.length; i++ ) 
			list.add(types[i].getName());
		return (String[]) list.toArray(new String[list.size()]);
	}
	
	private IServerType[] types = null;
	private IServerType[] getServerTypes() {
		if( types == null ) {
			ArrayList<IServerType> sorted = new ArrayList<IServerType>();
			ArrayList<IServerType> all = new ArrayList(
					Arrays.asList( ServerCore.getServerTypes()));
			Iterator<IServerType> i = all.iterator();
			IServerType t;
			while(i.hasNext()) {
				t = i.next();
				if( t.getId().startsWith("org.jboss.ide.eclipse.as.")) {//$NON-NLS-1$
					sorted.add(t);
					i.remove();
				}
			}
			sorted.addAll(all);
			types = (IServerType[]) sorted.toArray(new IServerType[sorted.size()]); 
		}
		return types;
	}
	
	private FormData createFormData(Object topStart, int topOffset,
			Object bottomStart, int bottomOffset, Object leftStart,
			int leftOffset, Object rightStart, int rightOffset) {
		FormData data = new FormData();

		if (topStart != null) {
			data.top = topStart instanceof Control ? new FormAttachment(
					(Control) topStart, topOffset) : new FormAttachment(
					((Integer) topStart).intValue(), topOffset);
		}

		if (bottomStart != null) {
			data.bottom = bottomStart instanceof Control ? new FormAttachment(
					(Control) bottomStart, bottomOffset) : new FormAttachment(
					((Integer) bottomStart).intValue(), bottomOffset);
		}

		if (leftStart != null) {
			data.left = leftStart instanceof Control ? new FormAttachment(
					(Control) leftStart, leftOffset) : new FormAttachment(
					((Integer) leftStart).intValue(), leftOffset);
		}

		if (rightStart != null) {
			data.right = rightStart instanceof Control ? new FormAttachment(
					(Control) rightStart, rightOffset) : new FormAttachment(
					((Integer) rightStart).intValue(), rightOffset);
		}

		return data;
	}

    public boolean performOk() {
    	String[] changed2 = (String[]) changed.toArray(new String[changed.size()]);
    	ArrayList<Fileset> list;
    	Fileset[] arr;
    	for( int i = 0; i < changed2.length; i++ ) {
    		list = cacheMap.get(changed2[i]);
    		arr = (Fileset[]) list.toArray(new Fileset[list.size()]);
			IPath fileToWrite = FilesetUtil.DEFAULT_FS_ROOT.append(changed2[i]);
			FilesetUtil.saveFilesets(fileToWrite.toFile(), arr);
    	}
    	changed.clear();
        return true;
    }

}
