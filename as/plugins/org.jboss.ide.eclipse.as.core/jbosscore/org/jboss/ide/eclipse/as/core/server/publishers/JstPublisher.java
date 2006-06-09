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

import java.net.URL;

import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jst.server.core.IEnterpriseApplication;
import org.eclipse.jst.server.core.IWebModule;
import org.eclipse.jst.server.core.PublishUtil;
import org.eclipse.jst.server.generic.core.internal.publishers.AbstractModuleAssembler;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.DeletedModule;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.eclipse.wst.server.core.util.ProjectModule;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.model.ServerProcessLog.ProcessLogEvent;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.core.util.ASDebug;
import org.osgi.framework.Bundle;

public class JstPublisher implements IJbossServerPublisher {

	public static final int BUILD = 1;
	public static final int PUBLISH = 2;
	public static final int BUILD_AND_PUBLISH = 3;
	public static final int UNDEPLOY = 4;
	
	private int state;
	private JBossServer server;
	public JstPublisher(JBossServer server) {
		this.server = server;
		state = IServer.PUBLISH_STATE_UNKNOWN;
	}

    public void publishModule(int kind, int deltaKind, IModule[] module,
            IProgressMonitor monitor) throws CoreException {
    	ASDebug.p("Publishing with kind,deltakind = "  + kind + "," + deltaKind, this);
    	checkClosed(module);
        if(ServerBehaviourDelegate.REMOVED == deltaKind){
            removeFromServer(module,monitor);
        } else {
        	JBossAntPublisher publisher = new JBossAntPublisher();
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

    private void removeFromServer(IModule[] module, IProgressMonitor monitor) throws CoreException {
    }

	public ProcessLogEvent[] getLogEvents() {
		// TODO Auto-generated method stub
		return new ProcessLogEvent[0];
	}

	public int getPublishState() {
		return state;
	}

	
	public static class JBossAntPublisher extends AbstractModuleAssembler {
		
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
	    
		private static boolean isModuleType(IModule module, String moduleTypeId){	
			if(module.getModuleType()!=null && moduleTypeId.equals(module.getModuleType().getId()))
				return true;
			return false;
		}

		
		public IStatus[] publish(IProgressMonitor monitor) throws CoreException {
			assemble(monitor);
        	String file = computeBuildFile();
			runAnt(file, BUILD_AND_PUBLISH, monitor);
			return null;
		}
		
		
		private void runAnt(String file, int action, IProgressMonitor monitor)throws CoreException {
			AntRunner runner = new AntRunner();
			runner.setBuildFileLocation(file);
			runner.setExecutionTargets(getTargets(module[0], BUILD_AND_PUBLISH));
			runner.setArguments(getArguments(module[0]));
			runner.run();
		}
		
		private String getArguments(IModule module) {
			String args = "";
			args += " -Dproject.working.dir=" + getProjectWorkingLocation().toString();
			args += " -Dmodule.name=" + module.getName();
			args += " -Dmodule.dir=" + getModuleWorkingDir().toString();
			args += " -Dserver.publish.dir=" + jbServer.getRuntimeConfiguration().getDeployDirectory();

			
			return args;
		}
		
		private static String[] getTargets(IModule module, int action ) {
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
			
			return new String[] { target };
		}
		
		public String computeBuildFile() {
			Bundle pluginBundle = JBossServerCorePlugin.getDefault().getBundle();
			try {
				URL url = FileLocator.resolve(pluginBundle.getEntry("/META-INF/jboss.publish.xml"));
				return url.getFile();
			} catch( Exception e ) {
			}
			return null;
		}
		
		public void assemble(IProgressMonitor monitor) throws CoreException {
			switch( assembleType ) {
			case WAR:
				assembleWar(monitor);
				break;
			case EAR:
				assembleEar(monitor);
				break;
			case OTHER:
				assembleOther(monitor);
				break;
			}
		}
		
		protected void assembleWar(IProgressMonitor monitor) throws CoreException {
			IPath parent =copyModule(module[0],monitor);
			IWebModule webModule = (IWebModule)module[0].loadAdapter(IWebModule.class, monitor);
			IModule[] childModules = webModule.getModules();
			for (int i = 0; i < childModules.length; i++) {
				IModule module = childModules[i];
				packModule(module, parent.append("WEB-INF").append("lib"));
			}
		}
		
		protected void assembleEar(IProgressMonitor monitor) throws CoreException {
			IPath parent =copyModule(fModule,monitor);
			IEnterpriseApplication earModule = (IEnterpriseApplication)fModule.loadAdapter(IEnterpriseApplication.class, monitor);
			IModule[] childModules = earModule.getModules();
			for (int i = 0; i < childModules.length; i++) {
				IModule module = childModules[i];
				packModule(module, parent);
			}
		}
		
		protected void assembleOther(IProgressMonitor monitor) throws CoreException {
			copyModule(module[0],monitor);		
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
