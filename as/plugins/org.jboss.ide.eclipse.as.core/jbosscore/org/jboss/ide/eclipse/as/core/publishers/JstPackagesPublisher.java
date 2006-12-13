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
package org.jboss.ide.eclipse.as.core.publishers;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jst.server.core.IEnterpriseApplication;
import org.eclipse.jst.server.core.IWebModule;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.DeletedModule;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.packages.core.Trace;
import org.jboss.ide.eclipse.packages.core.model.IPackage;
import org.jboss.ide.eclipse.packages.core.model.IPackageFileSet;
import org.jboss.ide.eclipse.packages.core.model.IPackageFileSetWorkingCopy;
import org.jboss.ide.eclipse.packages.core.model.IPackageFolder;
import org.jboss.ide.eclipse.packages.core.model.IPackageFolderWorkingCopy;
import org.jboss.ide.eclipse.packages.core.model.IPackageNode;
import org.jboss.ide.eclipse.packages.core.model.IPackageWorkingCopy;
import org.jboss.ide.eclipse.packages.core.model.PackagesCore;
import org.jboss.ide.eclipse.packages.core.model.internal.PackagesModel;
import org.jboss.ide.eclipse.packages.core.model.types.JARPackageType;

/**
 *  This class provides a default implementation for packaging different types of projects
 * @author rob.stryker@jboss.com
 */
public class JstPackagesPublisher implements IJBossServerPublisher {

	private static final String METAINF = "META-INF";
	private static final String WEBINF = "WEB-INF";
	private static final String LIB = "lib";
	private static final String WEBCONTENT = "WebContent";
	private static final String EARCONTENT = "EarContent";
	
	private int state;
	private JBossServer server;
	
	public JstPackagesPublisher(JBossServer server) {
		this.server = server;
		state = IServer.PUBLISH_STATE_NONE;
	}
	public int getPublishState() {
		return state;
	}


	public void publishModule(int kind, int deltaKind, int modulePublishState,
			IModule[] module, IProgressMonitor monitor) throws CoreException {
    	checkClosed(module);
        if(ServerBehaviourDelegate.REMOVED == deltaKind){
        	JstPackagesAssembler publisher = new JstPackagesAssembler();
            publisher.initialize(module,server);
            publisher.unpublish(monitor);
        } else if( ServerBehaviourDelegate.NO_CHANGE != deltaKind || kind == IServer.PUBLISH_FULL || kind == IServer.PUBLISH_CLEAN ){
        	// if there's no change, do nothing. Otherwise, on change or add, re-publish
        	JstPackagesAssembler publisher = new JstPackagesAssembler();
            publisher.initialize(module,server);
            publisher.publish(monitor);
        }
	}
    private void checkClosed(IModule[] module) throws CoreException {
    	for(int i=0;i<module.length;i++) {
    		if(module[i] instanceof DeletedModule) {	
                IStatus status = new Status(IStatus.ERROR,JBossServerCorePlugin.PLUGIN_ID,0, "Failure", null);
                throw new CoreException(status);
    		}
    	}
    }
    
    public static class JstPackagesAssembler {
		public static final int WAR = 1;
		public static final int EAR = 2;
		public static final int SAR = 3;
		public static final int OTHER = 4;
		
		
		private IModule[] module;
		private JBossServer jbServer;

		private int assembleType;
				
	    public void initialize(IModule[] module, JBossServer server) {
			this.module = module;
			this.jbServer = server;
			if( module.length == 1 ) {
				if(isModuleType(module[0], "jst.web")) {
					assembleType = WAR;
				} else if(isModuleType(module[0], "jst.ear")) {
					assembleType = EAR;
				} else {
					assembleType = OTHER;
				}
			}
	    }
	    
		private boolean isModuleType(IModule module, String moduleTypeId){	
			if(module.getModuleType()!=null && moduleTypeId.equals(module.getModuleType().getId()))
				return true;
			return false;
		}

		public IPackage[] getPackages() {
			IProject proj = module[0].getProject();
			if( proj != null) {
				IPackage[] projPackages = PackagesCore.getProjectPackages(proj, new NullProgressMonitor());
				if( projPackages.length > 0 ) {
					return projPackages;
				}
			}
			
			return new IPackage[] { createTopPackage(new NullProgressMonitor())};
		}
		
		public IPackage createTopPackage(IProgressMonitor monitor) {
			IPackage topLevel = null;
			switch( assembleType ) {
			case WAR:
				topLevel = createWarPackages(module[0], monitor); break;
			case EAR:
				topLevel = createEarPackages(module[0], monitor); break;
			case OTHER:
				topLevel = createOtherPackages(module[0], monitor); break;
			default: break;
			}
			return topLevel;
		}
		
		public IStatus[] publish(IProgressMonitor monitor) throws CoreException {
			IPackage topLevel = createTopPackage(monitor);
			if( topLevel != null ) {
				PackagesCore.buildPackage(topLevel, new NullProgressMonitor());
				PackagesModel.instance().removePackage((IPackage)topLevel);
			}
			return null;
		}
		
		public static IPackage createGenericIPackage(IModule module, String deployDirectory, String packageName) {
			try {

				IProject project = module.getProject();
				Assert.isNotNull(project);
				
				IJavaProject javaProject = JavaCore.create(project);
				Assert.isNotNull(javaProject);
				
				IPath sourcePath;
				try {
					sourcePath = javaProject.getOutputLocation();
				} catch (JavaModelException e) {
					e.printStackTrace();
					return null;
				}
				sourcePath = sourcePath.removeFirstSegments(1);
				IContainer sourcePathContainer = project.getFolder(sourcePath);
				return createGenericIPackage(module, deployDirectory, packageName, sourcePathContainer);
			} catch( Exception e ) {
				e.printStackTrace();
			}
			return null;
		} 
		
		public static IPackage createGenericIPackage(IModule module, String deployDirectory, String packageName, IContainer sourceContainer) {
			IProject project = module.getProject();
			Assert.isNotNull(project);
			IPackage jar = PackagesCore.createPackage(project, true);
			IPackageWorkingCopy jarWC = jar.createPackageWorkingCopy();
				
			if( deployDirectory != null ) {
				jarWC.setDestinationFolder(new Path(deployDirectory));
				jarWC.setExploded(false);
			}
			jarWC.setName(packageName);
			jar = jarWC.savePackage();
				
			IPackageFileSet classes = PackagesCore.createPackageFileSet(project);
			IPackageFileSetWorkingCopy classesWC = classes.createFileSetWorkingCopy();
			classesWC.setIncludesPattern("**/*");
			classesWC.setSourceContainer(sourceContainer);
			classes = classesWC.saveFileSet();
			jar.addChild(classes);
			return jar;
		}
		
		public IPackage createWarPackages(IModule module, IProgressMonitor monitor) {
			try {
				IProject proj = module.getProject();
				IPackage topLevel = createGenericIPackage(module, jbServer.getDeployDirectory(), proj.getName() + ".war");
				IPackageFolder webinf = addFolder(proj, topLevel, WEBINF);
				IPackageFolder metainf = addFolder(proj, topLevel, METAINF);
				IPackageFolder lib = addFolder(proj, metainf, LIB);
				addFileset(proj, metainf, WEBCONTENT + Path.SEPARATOR + METAINF, null);
				addFileset(proj, webinf, WEBCONTENT + Path.SEPARATOR + WEBINF, null);
				
				IWebModule webModule = (IWebModule)module.loadAdapter(IWebModule.class, monitor);
				IModule[] childModules = webModule.getModules();
				
				for (int i = 0; i < childModules.length; i++) {
					IModule child = childModules[i];
					JstPackagesAssembler assembler = new JstPackagesAssembler();
					assembler.initialize(new IModule[] {child}, jbServer);
					IPackage[] childPackages = assembler.getPackages();
					if( childPackages.length > 0 ) {
						for( int j = 0; j < childPackages.length; j++ ) {
							lib.addChild(childPackages[j]);
						}
					} else {
						lib.addChild(createGenericIPackage(child, null, child.getProject().getName() + ".jar"));
					}
				}
				return topLevel;
			} catch( Exception e ) {
				e.printStackTrace();
			}
			return null;
		}
		public IPackage createEarPackages(IModule module, IProgressMonitor monitor) {
			IProject proj = module.getProject();
			IContainer sourceContainer = proj.getFolder(EARCONTENT);

			IPackage topLevel = createGenericIPackage(module, jbServer.getDeployDirectory(), proj.getName() + ".ear", sourceContainer);
			addFileset(proj, topLevel, EARCONTENT, "**/*.*");
			
			// now add children
			IEnterpriseApplication earModule = (IEnterpriseApplication)module.loadAdapter(IEnterpriseApplication.class, monitor);
			IModule[] childModules = earModule.getModules();
			for( int i = 0; i < childModules.length; i++ ) {
				IModule child = childModules[i];
				JstPackagesAssembler assembler = new JstPackagesAssembler();
				assembler.initialize(new IModule[] {child}, jbServer);
				IPackage[] childPackages = assembler.getPackages();
				if( childPackages.length > 0 ) {
					for( int j = 0; j < childPackages.length; j++ ) {
						topLevel.addChild(childPackages[j]);
					}
				} else {
					topLevel.addChild(createGenericIPackage(child, null, child.getProject().getName() + ".jar"));
				}
			}
			
			PackagesCore.buildPackage(topLevel, new NullProgressMonitor());
			PackagesModel.instance().removePackage((IPackage)topLevel);
			return null;
		}
		public IPackage createOtherPackages(IModule module, IProgressMonitor monitor) {
			return createGenericIPackage(module, null, module.getName() + ".jar");
		}

		public IStatus[] unpublish(IProgressMonitor monitor) throws CoreException {
			String deployDir = jbServer.getDeployDirectory();
			String fileName = module[0].getProject().getName();
			String ext = ".jar";
			switch( assembleType ) {
				case WAR: ext = ".war"; break;
				case EAR: ext = ".ear"; break;
				case SAR: ext = ".sar"; break;
				case OTHER: ext = ".jar"; break;
				default: ext = ".jar"; break;
			}
			
			IPath filePath = new Path(deployDir).append(fileName + ext);
			filePath.toFile().delete();
			return null;
		}
		
		
		protected static IPackageFolder addFolder(IProject project, IPackageNode parent, String name) {
			IPackageFolder folder = PackagesCore.createPackageFolder(project);
			IPackageFolderWorkingCopy folderWC = folder.createFolderWorkingCopy();
			folderWC.setName(name);
			folder = folderWC.saveFolder();
			parent.addChild(folder);
			return folder;
		}
		protected static IPackageFileSet addFileset(IProject project, IPackageNode parent, String sourcePath, String includePattern) {
			IPackageFileSet fs = PackagesCore.createPackageFileSet(project);
			IPackageFileSetWorkingCopy fswc = fs.createFileSetWorkingCopy();
			Assert.isNotNull(project);
			IJavaProject javaProject = JavaCore.create(project);
			Assert.isNotNull(javaProject);
			IContainer sourceContainer = project.getFolder(new Path(sourcePath));

			fswc.setSourceContainer(sourceContainer);
			fswc.setIncludesPattern(  includePattern == null ?  "**/*" : includePattern );
			fs = fswc.saveFileSet();
			parent.addChild(fs);
			return fs;
		}
}


}
