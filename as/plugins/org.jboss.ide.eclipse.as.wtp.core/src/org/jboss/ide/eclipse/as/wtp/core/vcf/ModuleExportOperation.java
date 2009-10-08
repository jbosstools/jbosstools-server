/*******************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.jboss.ide.eclipse.as.wtp.core.vcf;

import static org.eclipse.jst.j2ee.datamodel.properties.IJ2EEComponentExportDataModelProperties.ARCHIVE_DESTINATION;
import static org.eclipse.jst.j2ee.datamodel.properties.IJ2EEComponentExportDataModelProperties.COMPONENT;
import static org.eclipse.jst.j2ee.datamodel.properties.IJ2EEComponentExportDataModelProperties.RUN_BUILD;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jst.j2ee.commonarchivecore.internal.exception.SaveFailureException;
import org.eclipse.jst.j2ee.internal.archive.operations.EJBArchiveOpsResourceHandler;
import org.eclipse.jst.j2ee.internal.plugin.LibCopyBuilder;
import org.eclipse.jst.j2ee.internal.project.ProjectSupportResourceHandler;
import org.eclipse.jst.jee.archive.ArchiveSaveFailureException;
import org.eclipse.jst.jee.archive.internal.ArchiveUtil;
import org.eclipse.jst.server.core.IEnterpriseApplication;
import org.eclipse.jst.server.core.IJ2EEModule;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
import org.eclipse.wst.common.frameworks.datamodel.AbstractDataModelOperation;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleType;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.eclipse.wst.server.core.util.ProjectModule;
import org.jboss.ide.eclipse.as.wtp.core.modules.IJBTModule;

public class ModuleExportOperation extends AbstractDataModelOperation {

	protected IProgressMonitor progressMonitor;
	private IVirtualComponent component;
	private IModule module;
	private IPath destinationPath;

	public ModuleExportOperation() {
		super();
	}

	public ModuleExportOperation(IDataModel model) {
		super(model);
	}
	
	protected final int REFRESH_WORK = 100;
	protected final int JAVA_BUILDER_WORK = 100;
	protected final int LIB_BUILDER_WORK = 100;
	protected final int EXPORT_WORK = 1000;
	protected final int CLOSE_WORK = 10;
	
	protected int computeTotalWork() {
		int totalWork = REFRESH_WORK;
		if (model.getBooleanProperty(RUN_BUILD)) {
			totalWork += JAVA_BUILDER_WORK + LIB_BUILDER_WORK;
		}
		totalWork += EXPORT_WORK + CLOSE_WORK;
		return totalWork;
	}
	
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		setComponent((IVirtualComponent) model.getProperty(COMPONENT));
		setDestinationPath(new Path(model.getStringProperty(ARCHIVE_DESTINATION)));
		
		try {
		    monitor.beginTask(ProjectSupportResourceHandler.getString(ProjectSupportResourceHandler.Exporting_archive, new Object [] { getDestinationPath().lastSegment() }), computeTotalWork());
            setProgressMonitor(monitor);
            if( component != null ) {
	    		try {
	    			// defect 240999
	    			component.getProject().refreshLocal(IResource.DEPTH_INFINITE, new SubProgressMonitor(monitor, REFRESH_WORK));
	    			if (model.getBooleanProperty(RUN_BUILD)) {
	    				runNecessaryBuilders(component, new SubProgressMonitor(monitor, JAVA_BUILDER_WORK + LIB_BUILDER_WORK));
	    			}
	    			export();
	    		} catch (Exception e) {
	    			monitor.worked(CLOSE_WORK);
	    			throw new ExecutionException(EJBArchiveOpsResourceHandler.Error_exporting__UI_ + component.getProject().getName(), e);
	    		}
            }
		} finally {
		    monitor.done();
		}
		
		return OK_STATUS;
	}

	protected void setProgressMonitor(IProgressMonitor newProgressMonitor) {
		progressMonitor = newProgressMonitor;
	}

	protected IProgressMonitor getProgressMonitor() {
		return progressMonitor;
	}

	private void setComponent(IVirtualComponent newComponent) {
		component = newComponent;
		setModule();
	}

	protected IVirtualComponent getComponent() {
		if (component == null)
			component = (IVirtualComponent) model.getProperty(COMPONENT);
		return component;
	}

	protected IPath getDestinationPath() {
		return destinationPath;
	}

	protected void setDestinationPath(IPath newDestinationPath) {
		destinationPath = newDestinationPath;
	}
	
	/*
	 * It's assumed that if the module is a "Project Module" then there's only 
	 * one of these per project. This might not be a great assumption, but 
	 * accepting the first ProjectModule which responds is how the behaviour works here. 
	 */
	protected void setModule() {
		if( component != null && component.getProject() != null) {
			IModule[] modules = ServerUtil.getModules(component.getProject());
			ModuleDelegate delegate;
			for( int i = 0; i < modules.length; i++ ) {
				delegate = (ModuleDelegate)modules[i].loadAdapter(ModuleDelegate.class, new NullProgressMonitor());
				if( delegate != null && delegate instanceof ProjectModule ) {
					module = modules[i];
					return;
				}
			}
		}
		module = null;
	}
	
	/* Return null to skip this child */
	protected /* abstract */ String getChildURI(IModule parent, IModule child) {
		IEnterpriseApplication app = (IEnterpriseApplication)parent.loadAdapter(IEnterpriseApplication.class, new NullProgressMonitor());
		if( app != null ) {
			return app.getURI(child);
		}
		IJBTModule mod = (IJBTModule)parent.loadAdapter(IJBTModule.class, new NullProgressMonitor());
		if( mod != null )
			return mod.getURI(child);
		return null;
	}
	
	protected void runNecessaryBuilders(IVirtualComponent component, IProgressMonitor monitor) throws CoreException {
		try{
			monitor.beginTask(null, JAVA_BUILDER_WORK + LIB_BUILDER_WORK);
			if(!component.isBinary()){
				IProject project = component.getProject();
				IProjectDescription description = project.getDescription();
				ICommand javaBuilder = getJavaCommand(description);
				if (javaBuilder != null) {
					project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, JavaCore.BUILDER_ID, javaBuilder.getArguments(), new SubProgressMonitor(monitor, JAVA_BUILDER_WORK));
				}
				ICommand libCopyBuilder = getLibCopyBuilder(description);
				if (null != libCopyBuilder) {
					project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, LibCopyBuilder.BUILDER_ID, libCopyBuilder.getArguments(), new SubProgressMonitor(monitor, LIB_BUILDER_WORK));
				}
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * Find the specific Java command amongst the build spec of a given description
	 */
	protected ICommand getJavaCommand(IProjectDescription description) throws CoreException {
		if (description == null) {
			return null;
		}

		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(JavaCore.BUILDER_ID)) {
				return commands[i];
			}
		}
		return null;
	}

	protected ICommand getLibCopyBuilder(IProjectDescription description) throws CoreException {
		if (description == null) {
			return null;
		}

		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(LibCopyBuilder.BUILDER_ID)) {
				return commands[i];
			}
		}
		return null;

	}

	public ISchedulingRule getSchedulingRule() {
		Set projs = gatherDependentProjects(getComponent(), new HashSet());
		ISchedulingRule combinedRule = null;
		IResourceRuleFactory ruleFactory = ResourcesPlugin.getWorkspace().getRuleFactory();
		for (Iterator iter = projs.iterator(); iter.hasNext();) {
			IProject proj = (IProject) iter.next();
			ISchedulingRule rule = ruleFactory.createRule(proj);
			combinedRule = MultiRule.combine(rule, combinedRule);
		}
		combinedRule = MultiRule.combine(ruleFactory.buildRule(), combinedRule);

		return combinedRule;
	}

	/* Only used by getSchedulingRule() as of now */
	private Set gatherDependentProjects(IVirtualComponent comp, Set projs) {
		if (!projs.contains(comp.getProject())) {
			projs.add(comp.getProject());
			IVirtualReference[] refs = comp.getReferences();
			for (int i = 0; i < refs.length; i++) {
				IVirtualReference refComp = refs[i];
				projs.addAll(gatherDependentProjects(refComp.getReferencedComponent(), projs));
			}
		}
		return projs;
	}
	
	

	protected void export() throws SaveFailureException, CoreException, InvocationTargetException, InterruptedException {
		if( module == null )
			throw new SaveFailureException(); // TODO add some real message
		try {
			File dest = getDestinationPath().toFile();
			File parent = dest.getParentFile(); 
			
			/* Prepare the streams */
			if (dest.exists() && dest.isDirectory()) {
				throw new IOException("The specified file: " + dest.getAbsolutePath() + " exists and is a directory");
			}
			if (parent != null)
				parent.mkdirs();
			java.io.OutputStream out = new java.io.FileOutputStream(dest);
			ZipStreamModuleSaveAdapterImpl saver = new ZipStreamModuleSaveAdapterImpl(out);
			
			
			/* 
			 * If we were to create a dual underlying model that would work for
			 * export and publish, this is where we'd do that and incorporate 
			 * preferences from the calling class
			 */
			
			ModuleDelegate moduleDelegate = (ModuleDelegate)module.loadAdapter(ModuleDelegate.class, new NullProgressMonitor());
			addResources(saver, moduleDelegate.members());
			addChildren(saver, module, moduleDelegate.getChildModules());
			saver.finish();
		} catch( Exception e ) {
			e.printStackTrace();
			throw new SaveFailureException();
		}
	}
	
	protected void addChildren(ZipStreamModuleSaveAdapterImpl saver, IModule parent, IModule[] children ) throws IOException, ArchiveSaveFailureException, CoreException {
		if( children != null ) {
			for( int i = 0; i < children.length; i++ ) {
				String path = getChildURI(parent, children[i]);
				ModuleDelegate childDelegate = (ModuleDelegate)children[i].
						loadAdapter(ModuleDelegate.class, new NullProgressMonitor());
				IJ2EEModule tempMod = (IJ2EEModule)children[i].loadAdapter(IJ2EEModule.class, new NullProgressMonitor());
				boolean isBinary = tempMod.isBinary();
				if( path != null ) {
					if( isBinary ) {
						addResources(saver, childDelegate.members());
					} else {
						ZipStreamModuleSaveAdapterImpl childSaver = saver.createNestedSaveAdapter(path);
						addResources(childSaver, childDelegate.members());
						addChildren(childSaver, children[i], childDelegate.getChildModules());
						childSaver.finish();
					}
				}
			}
		}
	}
	
	protected void addResources(ZipStreamModuleSaveAdapterImpl saver, IModuleResource[] resources) throws ArchiveSaveFailureException {
		for( int i = 0; i < resources.length; i++ ) {
			if( resources[i] instanceof IModuleFile ) {
				saver.save(resources[i]);
			} else if( resources[i] instanceof IModuleFolder ) {
				addResources(saver, ((IModuleFolder)resources[i]).members());
			}
		}
	}
	

	public static class ZipStreamModuleSaveAdapterImpl {
		protected OutputStream destinationStream;
		/** Used internally */
		protected ZipOutputStream zipOutputStream;

		public ZipStreamModuleSaveAdapterImpl(OutputStream out) {
			destinationStream = out;
			zipOutputStream = new ZipOutputStream(out);
		}

		public void close() throws IOException {
			getDestinationStream().close();
		}

		protected ZipStreamModuleSaveAdapterImpl createNestedSaveAdapter(String entry) throws IOException {
			ZipEntry nest = new ZipEntry(entry);
			getZipOutputStream().putNextEntry(nest);
			return new ZipStreamModuleSaveAdapterImpl(getZipOutputStream());
		}

		public void finish() throws IOException {
			getZipOutputStream().finish();
			//If this is not nested, close the stream to free up the resource
			//otherwise, don't close it because the parent may not be done
			if (!(getDestinationStream() instanceof ZipOutputStream))
				getDestinationStream().close();
		}

		public java.io.OutputStream getDestinationStream() {
			return destinationStream;
		}

		protected java.util.zip.ZipOutputStream getZipOutputStream() {
			return zipOutputStream;
		}

		protected void save(IModuleResource resource) throws ArchiveSaveFailureException {
			if( resource instanceof IModuleFile ) {
				File f = (File)((IModuleFile)resource).getAdapter(File.class);
				if( f == null ) {
					IFile ifile = (IFile)((IModuleFile)resource).getAdapter(IFile.class);
					if( ifile != null ) 
						f = ifile.getLocation().toFile();
				}
				saveFile(f, resource.getModuleRelativePath().append(resource.getName()));
			}
		}
		
		protected void saveFile(File f, IPath entryPath) throws ArchiveSaveFailureException{
			try {
				ZipEntry entry = new ZipEntry(entryPath.toString());
				if (f.lastModified() > 0)
					entry.setTime(f.lastModified());
				getZipOutputStream().putNextEntry(entry);
				ArchiveUtil.copy(new FileInputStream(f), getZipOutputStream());
				getZipOutputStream().closeEntry();
			} catch (IOException e) {
				throw new ArchiveSaveFailureException(e);
			}
		}
	}
	
}
