/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ide.eclipse.as.core.server.publishers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.eclipse.ant.internal.ui.IAntUIConstants;
import org.eclipse.ant.internal.ui.launchConfigurations.IAntLaunchConfigurationConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jst.server.core.IEnterpriseApplication;
import org.eclipse.jst.server.core.IWebModule;
import org.eclipse.jst.server.core.PublishUtil;
import org.eclipse.jst.server.generic.core.internal.CorePlugin;
import org.eclipse.jst.server.generic.core.internal.publishers.ModulePackager;
import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleType;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.DeletedModule;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.eclipse.wst.server.core.util.ProjectModule;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.model.ModuleModel;
import org.jboss.ide.eclipse.as.core.model.ServerProcessLog;
import org.jboss.ide.eclipse.as.core.model.ServerProcessLog.ProcessLogEvent;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.core.server.JBossServerBehavior.PublishLogEvent;
import org.jboss.ide.eclipse.as.core.util.ASDebug;
import org.osgi.framework.Bundle;

public class JstPublisher implements IJbossServerPublisher {

	public static final int BUILD = 1;
	public static final int PUBLISH = 2;
	public static final int BUILD_AND_PUBLISH = 3;
	public static final int UNDEPLOY = 4;
	
	public static final String BUILD_PROPERTIES = "_BUILD_PROPERTIES_";
	public static final String BUILD_FILE = "_BUILD_FILE_";
	public static final String ANT_TARGETS = "_ANT_TARGETS_";
	public static final String MODULE_TYPE = "_MODULE_TYPE_";
	
	private int state;
	private JBossServer server;
	private ProcessLogEvent log;
	private PublishLogEvent event;
	
	public JstPublisher(JBossServer server) {
		this.server = server;
		state = IServer.PUBLISH_STATE_UNKNOWN;
		this.log = new ServerProcessLog.ProcessLogEvent(ProcessLogEvent.UNKNOWN);
	}

    public void publishModule(int kind, int deltaKind, IModule[] module,
            IProgressMonitor monitor) throws CoreException {
		// delta = [no_change, added, changed, removed] = [0,1,2,3]
		// kind = [incremental, full, auto, clean] = [1,2,3,4]
    	ASDebug.p("Publishing with kind,deltakind = "  + kind + "," + deltaKind, this);
    	checkClosed(module);
        if(ServerBehaviourDelegate.REMOVED == deltaKind){
        	JBossAntPublisher publisher = new JBossAntPublisher();
            publisher.initialize(module,server);
            publisher.unpublish(monitor);
        } else if( ServerBehaviourDelegate.NO_CHANGE != deltaKind ){
        	// if there's no change, do nothing. Otherwise, on change or add, re-publish
        	JBossAntPublisher publisher = new JBossAntPublisher();
            publisher.initialize(module,server);
            publisher.publish(monitor);
			ModuleModel.getDefault().getDeltaModel().setDeltaSeen(module[0], server.getServer().getId());
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

	public ProcessLogEvent[] getLogEvents() {
		return log.getChildren();
	}

	public int getPublishState() {
		return state;
	}

	
	public class JBossAntPublisher {
		
		public static final int WAR = 1;
		public static final int EAR = 2;
		public static final int OTHER = 3;
		
		
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

		
		public IStatus[] publish(IProgressMonitor monitor) throws CoreException {

			event = new PublishLogEvent(PublishLogEvent.PUBLISH);
			event.setProperty(PublishLogEvent.MODULE_NAME, module[0].getName());
			log.addChild(event);
			event.setProperty(MODULE_TYPE, module[0].getModuleType().getId());

			
			assemble(monitor);
        	String file = computeBuildFile();
			runAnt(file, BUILD_AND_PUBLISH, monitor);
			state = IServer.PUBLISH_STATE_NONE;
			
			
			return null;
		}
		
		public IStatus[] unpublish(IProgressMonitor monitor) throws CoreException {
			event = new PublishLogEvent(PublishLogEvent.UNPUBLISH);
			event.setProperty(PublishLogEvent.MODULE_NAME, module[0].getName());
			log.addChild(event);
        	String file = computeBuildFile();
        	runAnt(file, UNDEPLOY, monitor);
			return null;
		}
		               
		               
		private void runAnt(String file, int action, IProgressMonitor monitor)throws CoreException {
			
			String targets = getTargets(module[0], action);
			
			Properties props = new Properties();
			props.put("project.working.dir", getProjectWorkingLocation().toString());
			props.put("module.name", module[0].getName());
			props.put("module.dir", getModuleWorkingDir().toString());
			props.put("server.publish.dir", jbServer.getAttributeHelper().getDeployDirectory());

			
			// Log
			event.setProperty(ANT_TARGETS, targets);
			event.setProperty(BUILD_PROPERTIES, props);
			
			
			ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType type = launchManager.getLaunchConfigurationType(
					IAntLaunchConfigurationConstants.ID_ANT_LAUNCH_CONFIGURATION_TYPE);
			
			if(type==null){
				IStatus s = new Status(IStatus.ERROR,JBossServerCorePlugin.PLUGIN_ID,0,
						"Ant Launcher Missing",null);
				throw new CoreException(s);
			}
			
			ILaunchConfigurationWorkingCopy wc= type.newInstance(null,"module publisher"); 
			wc.setContainer(null);
			wc.setAttribute(IExternalToolConstants.ATTR_LOCATION, file);
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER,"org.eclipse.ant.ui.AntClasspathProvider"); 
			wc.setAttribute(IAntLaunchConfigurationConstants.ATTR_ANT_TARGETS,targets);
			wc.setAttribute(IAntLaunchConfigurationConstants.ATTR_ANT_PROPERTIES,props);
			wc.setAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND,false);
			wc.setAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE,true);
			wc.setAttribute(IDebugUIConstants.ATTR_PRIVATE,true);
			
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH_PROVIDER, "org.eclipse.ant.ui.AntClasspathProvider"); 
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "org.eclipse.ant.internal.ui.antsupport.InternalAntRunner"); 
			//wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, args);
			wc.setAttribute(DebugPlugin.ATTR_PROCESS_FACTORY_ID, IAntUIConstants.REMOTE_ANT_PROCESS_FACTORY_ID);
			
			ILaunchConfiguration launchConfig = wc.doSave();
	        ILaunch launch = launchConfig.launch("run",monitor);
//			IProcess[] p = launch.getProcesses();
//			IStreamListener listener = new IStreamListener() {
//				public void streamAppended(String text, IStreamMonitor monitor) {
//				}
//			};
//			//p[0].getStreamsProxy().getOutputStreamMonitor().addListener(listener);
//			//p[0].getStreamsProxy().getErrorStreamMonitor().addListener(listener);
//			
			
		}
		
		
		private String getTargets(IModule module, int action ) {
			String moduleType = module.getModuleType().getId();
			String targetType = "";
			String target = "";
			if("jst.web".equals(moduleType)) {
				targetType = "j2ee.web";
			} else if( "jst.ear".equals(moduleType)) {
				targetType = "j2ee.ear";
			} else if( "jst.ejb".equals(moduleType)) {
				targetType = "j2ee.ejb";
			}
			
			if( action == BUILD_AND_PUBLISH ) {
				target = "deploy." + targetType;
			} else if( action == UNDEPLOY ) {
				target = "undeploy." + targetType;
			}
			
			return target;
		}
		
		public String computeBuildFile() {
			Bundle pluginBundle = JBossServerCorePlugin.getDefault().getBundle();
			try {
				URL url = FileLocator.resolve(pluginBundle.getEntry("/META-INF/jboss.publish.xml"));
				event.setProperty(BUILD_FILE, url.toString());
				return url.getFile();
			} catch( Exception e ) {
			}
			return null;
		}
		
		 
		private String getDUName(IModule module) {
			IModuleType moduleType = module.getModuleType();

			if (moduleType == null)
				return module.getName() + ".jar";

			if ("jst.web".equals(moduleType.getId())) {

				IWebModule webmodule = (IWebModule) module.loadAdapter(
						IWebModule.class, null);
				return webmodule.getURI(module);
			}

			if ("jst.ear".equals(moduleType.getId()))
				return module.getName() + ".ear";

			if ("jst.connector".equals(moduleType.getId()))
				return module.getName() + ".rar";

			return module.getName() + ".jar";
		}
		
		public IPath assemble(IProgressMonitor monitor) throws CoreException {
			switch( assembleType ) {
			case WAR:
				return assembleWar(monitor);
			case EAR:
				return assembleEar(monitor);
			case OTHER:
				return assembleOther(monitor);
			}
			return null;
		}
		
		protected IPath assembleWar(IProgressMonitor monitor) throws CoreException {
			IPath parent =copyModule(module[0],monitor);
			IWebModule webModule = (IWebModule)module[0].loadAdapter(IWebModule.class, monitor);
			IModule[] childModules = webModule.getModules();
			IPath webPath = parent.append("WEB-INF").append("lib");
			
			for (int i = 0; i < childModules.length; i++) {
				IModule module = childModules[i];
				packModule(module, getDUName(module), webPath);
			}
			
			return webPath;
		}
		
		protected IPath assembleEar(IProgressMonitor monitor) throws CoreException {
			IPath parent =copyModule(module[0],monitor);
			IEnterpriseApplication earModule = (IEnterpriseApplication)module[0].loadAdapter(IEnterpriseApplication.class, monitor);
			IModule[] childModules = earModule.getModules();
			
			for (int i = 0; i < childModules.length; i++) {
				IModule module = childModules[i];
				packModule(module, getDUName(module), parent);
			}
			return parent;
		}
		
		protected void packModule(IModule module, String deploymentUnitName, IPath destination)throws CoreException {
			
			
			String dest = destination.append(deploymentUnitName).toString();
			ModulePackager packager = null;
			try {
				packager = new ModulePackager(dest, false);
				ProjectModule pm = (ProjectModule) module.loadAdapter(ProjectModule.class, null);
				IModuleResource[] resources = pm.members();
				for (int i = 0; i < resources.length; i++) {
					doPackModule(resources[i], packager);
				}
			} catch (IOException e) {
				IStatus status = new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, 0,
						"unable to assemble module", e); //$NON-NLS-1$
				throw new CoreException(status);
			}
			finally{
				try{
					packager.finished();
				}
				catch(IOException e){
					//unhandled
				}
			}
		}

		private void doPackModule(IModuleResource resource, ModulePackager packager) throws CoreException, IOException{
				if (resource instanceof IModuleFolder) {
					IModuleFolder mFolder = (IModuleFolder)resource;
					IModuleResource[] resources = mFolder.members();

					packager.writeFolder(resource.getModuleRelativePath().append(resource.getName()).toPortableString());

					for (int i = 0; resources!= null && i < resources.length; i++) {
						doPackModule(resources[i], packager);
					}
				} else {
					String destination = resource.getModuleRelativePath().append(resource.getName()).toPortableString();
					IFile file = (IFile) resource.getAdapter(IFile.class);
					if (file != null)
						packager.write(file, destination);
					else {
						File file2 = (File) resource.getAdapter(File.class);
						packager.write(file2, destination);
					}
				}
		}

		
		protected IPath assembleOther(IProgressMonitor monitor) throws CoreException {
			copyModule(module[0],monitor);
			return null;
		}
	    
		
		
		/**
		 * Unchanged from superclass. 
		 */
		protected IPath copyModule(IModule module,IProgressMonitor monitor)throws CoreException{
			ProjectModule pm =(ProjectModule)module.loadAdapter(ProjectModule.class, monitor);
			IPath to = getProjectWorkingLocation().append(pm.getId());
			PublishUtil.smartCopy(pm.members(), to, monitor);
			return to;
		}
		
		/**
		 * Changed just to acknowledge I am not REALLY a GenericServer
		 * @return
		 */
		private IPath getProjectWorkingLocation(){
			return ServerPlugin.getInstance().getTempDirectory(jbServer.getServer().getId());
		}
		private IPath getModuleWorkingDir(){
			return getProjectWorkingLocation().append(module[0].getProject().getName());
		}

		
	}
	
}
