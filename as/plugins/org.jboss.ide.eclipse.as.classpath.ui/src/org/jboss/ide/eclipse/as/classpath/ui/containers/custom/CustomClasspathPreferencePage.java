/*******************************************************************************
 * Copyright (c) 2011-2104 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.classpath.ui.containers.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.util.CoreUtility;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.common.project.facet.core.FacetedProjectFramework;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.archives.webtools.filesets.Fileset;
import org.jboss.ide.eclipse.archives.webtools.filesets.FilesetLabelProvider;
import org.jboss.ide.eclipse.as.classpath.core.ClasspathCorePlugin;
import org.jboss.ide.eclipse.as.classpath.core.runtime.CustomRuntimeClasspathModel;
import org.jboss.ide.eclipse.as.classpath.core.runtime.IRuntimePathProvider;
import org.jboss.ide.eclipse.as.classpath.ui.Messages;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.preferences.ServerTypePreferencePage;
import org.osgi.service.prefs.BackingStoreException;

/*
 * Should be unified with DefaultFilesetPreferencePage
 * needs abstract superclass in jboss.as.ui plugin
 */
public class CustomClasspathPreferencePage extends ServerTypePreferencePage {
	private static final String LAST_SELECTED_RUNTIME_TYPE = "org.jboss.ide.eclipse.as.classpath.ui.containers.custom.CustomClasspathPreferencePage.LAST_RUNTIME_SELECTED"; //$NON-NLS-1$
	
	protected Control createContents(Composite parent) {
		rootComp = new CustomClasspathPreferenceComposite(parent, SWT.NONE);
		rootComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		rootComp.layout();
		return rootComp;
	}
	public boolean performOk() {
		String[] changed2 = rootComp.getChanged();
		ArrayList<Object> list;
		IRuntimePathProvider[] arr;
		final ArrayList<IProject> projectsNeedRefresh = new ArrayList<IProject>();
		for( int i = 0; i < changed2.length; i++ ) {
			String runtimeId = changed2[i];
			IRuntimeType rt = ServerCore.findRuntimeType(runtimeId);
			list = rootComp.getDataForComboSelection(changed2[i]);
			arr = (IRuntimePathProvider[]) list.toArray(new IRuntimePathProvider[list.size()]);
			CustomRuntimeClasspathModel.savePathProviders(rt, arr);
			clearRuntimeTypeCachedClasspathEntries(rt);
			IProject[] projectsTargeting = findProjectsTargeting(rt);
			projectsNeedRefresh.addAll(Arrays.asList(projectsTargeting));
		}
		
		// Save the recently selected
		String lastSelected = rootComp.getCurrentId();
		IEclipsePreferences prefs = new InstanceScope().getNode(JBossServerUIPlugin.PLUGIN_ID);
		prefs.put(LAST_SELECTED_RUNTIME_TYPE, lastSelected);
		try {
			prefs.flush();
		} catch(BackingStoreException e) {
			// IGNORE this since it is only a setting to remember what was the last selected
			// runtime. This is extremely not necessary to log or inform the user about. 
		}
		
		MessageDialog dialog= new MessageDialog(getShell(), 
				Messages.CustomClasspathsSettingsChanged, null, 
				Messages.CustomClasspathsRequiresRebuild,
				MessageDialog.QUESTION, new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL }, 2);
		
		int res= dialog.open();
		if (res == 0) {
			Job j = new WorkspaceJob(Messages.CustomClasspathsWorkspaceJob) {
				public IStatus runInWorkspace(IProgressMonitor monitor) {
					Iterator<IProject> i = projectsNeedRefresh.iterator();
					monitor.beginTask(Messages.CustomClasspathsWorkspaceJob, projectsNeedRefresh.size());
					while(i.hasNext()) {
						IJavaProject jp = JavaCore.create(i.next());
						try {
							// Must reset the classpath to actually force both views and models to refresh
							// A full build is not enough
							jp.setRawClasspath(jp.getRawClasspath(), new NullProgressMonitor());
						} catch( JavaModelException jme ) {
							return jme.getStatus();
						}
						CoreUtility.getBuildJob(jp.getProject()).schedule();
					}
					monitor.done();
					return Status.OK_STATUS;
				}
			};
			j.setRule(ResourcesPlugin.getWorkspace().getRoot());
			j.schedule();
		}
		rootComp.clearChanged();
	    return true;
	} 
	
	/* Clear the cached entries for this runtime */
	private void clearRuntimeTypeCachedClasspathEntries(IRuntimeType rt) {
		ClasspathCorePlugin.clearCachedClasspathEntries(rt);
	}
	
	private IProject[] findProjectsTargeting(IRuntimeType rt) {
		ArrayList<IProject> matching = new ArrayList<IProject>();
		IProject[] allProjs = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for( int i = 0; i < allProjs.length; i++ ) {
			try {
				if( !allProjs[i].exists() || !allProjs[i].isOpen())
					continue;
				if(FacetedProjectFramework.isFacetedProject(allProjs[i])) {
					IFacetedProject fp = ProjectFacetsManager.create(allProjs[i]);
					org.eclipse.wst.common.project.facet.core.runtime.IRuntime primary = fp.getPrimaryRuntime();
					if( primary != null ) {
						IRuntime wstRuntime = ServerCore.findRuntime(primary.getName());
						if( wstRuntime.getRuntimeType().getId().equals(rt.getId()))
							matching.add(allProjs[i]);
					}
				}
			} catch(CoreException ce) {
				// Exception thrown if project does not exist or is closed,
				// can ignore safely because we checked that, and also, 
				// if this project is erroring, we do not want to error every time
				// this is checked. Simply recognize this project is not suitable 
			}
		}
		return matching.toArray(new IProject[matching.size()]);
	}
	
	public class CustomClasspathPreferenceComposite extends AbstractComboDataPreferenceComposite {
		public CustomClasspathPreferenceComposite(Composite parent, int style) {
			super(parent, style);
		}
		protected void initializeSelection() {
			IEclipsePreferences prefs = new InstanceScope().getNode(JBossServerUIPlugin.PLUGIN_ID);
			String last =prefs.get(LAST_SELECTED_RUNTIME_TYPE, null);
			if( last == null )
				super.initializeSelection();
			else {
				IRuntimeType[] types = getRuntimeTypes();
				for( int i = 0; i < types.length; i++ ) {
					if( types[i].getId().equals(last)) {
						combo.select(i);
						return;
					}
				}
			}
		}
		
		protected LabelProvider getLabelProvider() {
			return new LabelProvider(){
				public Image getImage(Object element) {
		    		return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
			    }
			    public String getText(Object element) {
			    	if( element instanceof IRuntimePathProvider ) {
			    		return ((IRuntimePathProvider)element).getDisplayString();
			    	}
			        return super.getText(element);
			    }
			};
		}
		public String getDescriptionLabel() {
			return "Set classpath filesets for this runtime type";
		}
		
		protected Object[] getCurrentComboSelectionDefaultDataModel() {
			String id = getCurrentId();
			IRuntimeType rtType = ServerCore.findRuntimeType(id);
			IRuntimePathProvider[] sets = CustomRuntimeClasspathModel.getInstance().getDefaultEntries(rtType);
			return sets;
		}

		protected IRuntimePathProvider[] getCurrentSelectionDataModel() {
			String id = getCurrentId();
			ArrayList<Object> list = new ArrayList<Object>();
			if( id != null ) {
				list = getDataForComboSelection(id);
				if( list == null ) {
					IRuntimeType rtType = ServerCore.findRuntimeType(id);
					if( rtType != null ) {
						IRuntimePathProvider[] sets = CustomRuntimeClasspathModel.getInstance().getEntries(rtType);
						list = new ArrayList<Object>();
						list.addAll(Arrays.asList(sets));
						cacheMap.put(id, list);
					}
				}
			}
			return (IRuntimePathProvider[]) list.toArray(new IRuntimePathProvider[list.size()]);
		}

		protected String getAllOptionString() {
			return "All Runtime Types";
		}
		
		private IRuntimeType[] types = null;
		private IRuntimeType[] getRuntimeTypes() {
			if( types == null ) {
				ArrayList<IRuntimeType> retval = new ArrayList<IRuntimeType>();
				ArrayList<IRuntimeType> all = new ArrayList<IRuntimeType>(
						Arrays.asList( ServerCore.getRuntimeTypes()));
				if( !getAllServerTypes()) {
					Iterator<IRuntimeType> i = all.iterator();
					IRuntimeType t;
					while(i.hasNext()) {
						t = i.next();
						if( !t.getId().startsWith("org.jboss.ide.eclipse.as.")) {//$NON-NLS-1$
							i.remove();
						}
					}
				}
				retval = all;
				Collections.sort(retval, new Comparator<IRuntimeType>(){
					public int compare(IRuntimeType o1, IRuntimeType o2) {
						return o1.getName().compareTo(o2.getName());
					}});
				types = (IRuntimeType[]) all.toArray(new IRuntimeType[all.size()]); 
			}
			return types;
		}
		protected boolean showAllOption() {
			return false;
		}
		protected String getIdAtIndex(int index) {
			IRuntimeType type = getRuntimeTypes()[index];
			String id = type.getId();
			return id;
		}
		
		@Override
		protected void addPressed() {
			String id = getCurrentId();
			IRuntimeType rtt = id == null ? null : ServerCore.findRuntimeType(id);
			RuntimeClasspathProviderWizard wizard = new RuntimeClasspathProviderWizard(rtt);
			WizardDialog wd = new WizardDialog(addButton.getShell(), wizard);
			if( wd.open() == Window.OK) {
				IRuntimePathProvider ret = wizard.getRuntimePathProvider();
				if( ret != null )
					addObject(ret);
			}
		}
		
		@Override
		protected boolean getAllServerTypes() {
			return false;
		}

		@Override
		protected String[] getComboItemNames() {
			IRuntimeType[] types = getRuntimeTypes();
			String[] names = new String[types.length];
			for( int i = 0; i < types.length; i++ ) 
				names[i] = types[i].getName();
			return names;
		}
	}
}
