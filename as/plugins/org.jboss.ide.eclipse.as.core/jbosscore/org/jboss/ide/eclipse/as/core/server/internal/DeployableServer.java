/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jst.j2ee.project.WebUtilities;
import org.eclipse.jst.server.core.IWebModule;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerPort;
import org.eclipse.wst.server.core.model.ServerDelegate;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IDeployableServerBehaviour;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.IMultiModuleURLProvider;
import org.jboss.ide.eclipse.as.core.util.RuntimeUtils;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;
import org.jboss.ide.eclipse.as.wtp.core.util.ServerModelUtilities;

public class DeployableServer extends ServerDelegate implements IDeployableServer, IMultiModuleURLProvider {

	public DeployableServer() {
	}

	protected void initialize() {
	}
	
	public void setDefaults(IProgressMonitor monitor) {
		IRuntime rt = getServer().getRuntime();
		if( rt != null ) {
			getServerWorkingCopy().setName(ServerUtil.getDefaultServerName(rt));
		} else {
			getServerWorkingCopy().setName(ServerUtil.getDefaultServerName(getServer().getServerType().getName()));
		}
	}
	
	public void importRuntimeConfiguration(IRuntime runtime, IProgressMonitor monitor) throws CoreException {
	}

	public void saveConfiguration(IProgressMonitor monitor) throws CoreException {
	}

	public void configurationChanged() {
	}
	
	/*
	 * Abstracts to implement
	 */
	public IStatus canModifyModules(IModule[] add, IModule[] remove) {
		return Status.OK_STATUS;
	}

    public IModule[] getRootModules(IModule module) throws CoreException {
        IStatus status = canModifyModules(new IModule[] { module }, null);
        if (status != null && !status.isOK())
            throw  new CoreException(status);
        IModule[] parents = ServerModelUtilities.getParentModules(getServer(), module);
        if(parents.length>0)
        	return parents;
        return new IModule[] { module };
    }

	public IModule[] getChildModules(IModule[] module) {
		return ServerModelUtilities.getChildModules(module);
	}

	public ServerPort[] getServerPorts() {
		return new ServerPort[0];
	}
	
	public void modifyModules(IModule[] add, IModule[] remove,
			IProgressMonitor monitor) throws CoreException {
	}
	
	
	public String getDeployFolder() {
		return ServerUtil.makeGlobal(getServer().getRuntime(), new Path(getAttribute(DEPLOY_DIRECTORY, ""))).toString(); //$NON-NLS-1$
	}
	
	public void setDeployFolder(String folder) {
		setAttribute(DEPLOY_DIRECTORY, ServerUtil.makeRelative(getServer().getRuntime(), new Path(folder)).toString());
	}
	
	public String getTempDeployFolder() {
		return ServerUtil.makeGlobal(getServer().getRuntime(), new Path(getAttribute(TEMP_DEPLOY_DIRECTORY, ""))).toString(); //$NON-NLS-1$
	} 
	
	public void setTempDeployFolder(String folder) {
		setAttribute(TEMP_DEPLOY_DIRECTORY, ServerUtil.makeRelative(getServer().getRuntime(), new Path(folder)).toString());
	}
	
	public void setDeployLocationType(String type) {
		setAttribute(DEPLOY_DIRECTORY_TYPE, type);
	}
	
	public String getDeployLocationType() {
		return getAttribute(DEPLOY_DIRECTORY_TYPE, DEPLOY_CUSTOM);
	}
	
	public void setZipWTPDeployments(boolean val) {
		setAttribute(ZIP_DEPLOYMENTS_PREF, val);
	}
	public boolean zipsWTPDeployments() {
		return getAttribute(ZIP_DEPLOYMENTS_PREF, false);
	}

	protected Pattern defaultFilePattern = Pattern.compile(
			getDefaultModuleRestartPattern(),
			Pattern.CASE_INSENSITIVE);
	
	/* Needs to be public so UI can access this */
	public String getDefaultModuleRestartPattern() {
		return IDeployableServer.ORG_JBOSS_TOOLS_AS_RESTART_DEFAULT_FILE_PATTERN;
	}
	
	public void setRestartFilePattern(String filepattern) {
		setAttribute(ORG_JBOSS_TOOLS_AS_RESTART_FILE_PATTERN, filepattern);
	}
	
	public Pattern getRestartFilePattern() {
		return getCompiledRestartPattern();
	}
	
	private Pattern getCompiledRestartPattern() {
		// ensure it's set properly from the saved attribute
		String currentPattern = getAttribute(ORG_JBOSS_TOOLS_AS_RESTART_FILE_PATTERN, (String)null);
		try {
			return currentPattern == null ? defaultFilePattern :  
				Pattern.compile(currentPattern, Pattern.CASE_INSENSITIVE);
		} catch(PatternSyntaxException pse) {
			JBossServerCorePlugin.log("Could not set restart file pattern to: " + currentPattern, pse); //$NON-NLS-1$
			// avoid errors over and over
			return defaultFilePattern;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jboss.ide.eclipse.as.core.server.attributes.IDeployableServer#getAttributeHelper()
	 */
	public ServerAttributeHelper getAttributeHelper() {
		IServerWorkingCopy copy = getServerWorkingCopy();
		if( copy == null ) {
			copy = getServer().createWorkingCopy();
		}
		return new ServerAttributeHelper(getServer(), copy);
	}

	// only used for xpaths and is a complete crap hack ;) misleading, too
	public String getConfigDirectory() {
		return getDeployFolder();
	}
	
	public IJBossServerRuntime getRuntime() {
		return RuntimeUtils.getJBossServerRuntime(getServer());
	}
	
	public boolean hasJMXProvider() {
		return false;
	}
	
	protected int getWebPort() {
		return 80;
	}
	public URL getModuleRootURL(IModule module) {
		return getModuleRootURL(module, getServer().getHost(), getWebPort(), null);
	}
	public URL getModuleRootURL(IModule[] module) {
		if( module.length == 2) {
			String contextRoot = WebUtilities.getServerContextRoot(module[1].getProject(),
					module[0].getProject());
			return getModuleRootURL(module[1], getServer().getHost(), getWebPort(), contextRoot);
		} else {
			return module.length > 0 ? getModuleRootURL(module[0]) : null;
		}
	}
	public URL getModuleRootURL(IModule module, String contextRoot) {
		return getModuleRootURL(module, getServer().getHost(), getWebPort(), contextRoot);
	}

	public static URL getModuleRootURL(IModule module, String host, int port, String contextRoot) {
        if (module == null || module.loadAdapter(IWebModule.class,null)==null )
			return null;
        
        IWebModule webModule =(IWebModule)module.loadAdapter(IWebModule.class,null);
		String host2 = ServerUtil.formatPossibleIpv6Address(host); 
        String url = host2;
		if( !url.startsWith("http://") && !url.startsWith("https://") ) { //$NON-NLS-1$ //$NON-NLS-2$
			url = "http://"+host2; //$NON-NLS-1$
		}
		if (port != 80)
			url += ":" + port; //$NON-NLS-1$

		if( contextRoot == null ) {
			contextRoot = webModule.getContextRoot();
		}
		if( !contextRoot.equals("/") && !contextRoot.equals("./")) //$NON-NLS-1$ //$NON-NLS-2$
			url += contextRoot.startsWith("/") ? contextRoot : "/"+contextRoot; //$NON-NLS-1$ //$NON-NLS-2$
		if (!url.endsWith("/")) //$NON-NLS-1$
			url += "/"; //$NON-NLS-1$

		try {
			return new URL(url);
		} catch( MalformedURLException murle) { return null; }
	}

	protected IJBossServerPublishMethod createPublishMethod() {
		IDeployableServerBehaviour beh = ServerConverter.getDeployableServerBehavior(getServer());
		if( beh != null ) {
			IJBossServerPublishMethod method = ((DeployableServerBehavior)beh).createPublishMethod();
			return method;
		}
		return null;
	}
	public IPath getDeploymentLocation(IModule[] module, boolean deep) {
		return PublishUtil.getDeployPath(createPublishMethod(), module, this, deep);
	}
	public IPath getTempDeploymentLocation(IModule[] module, boolean deep) {
		return PublishUtil.getTempDeployPath(createPublishMethod(), module, this, deep);
	}
}
