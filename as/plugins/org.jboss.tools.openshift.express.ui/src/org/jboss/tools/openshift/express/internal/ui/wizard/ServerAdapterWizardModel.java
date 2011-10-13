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
package org.jboss.tools.openshift.express.internal.ui.wizard;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.egit.core.op.CloneOperation;
import org.eclipse.egit.core.op.ConnectProviderOperation;
import org.eclipse.egit.ui.Activator;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.InvalidMergeHeadsException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.tools.common.ui.databinding.ObservableUIPojo;
import org.jboss.tools.openshift.express.client.IApplication;
import org.jboss.tools.openshift.express.client.IUser;
import org.jboss.tools.openshift.express.client.OpenshiftException;
import org.jboss.tools.openshift.express.internal.core.behaviour.ExpressServerUtils;
import org.jboss.tools.openshift.express.internal.ui.OpenshiftUIActivator;
import org.jboss.tools.openshift.express.internal.ui.wizard.projectimport.GeneralProjectImportOperation;
import org.jboss.tools.openshift.express.internal.ui.wizard.projectimport.MavenProjectImportOperation;

/**
 * @author André Dietisheim <adietish@redhat.com>
 */
public class ServerAdapterWizardModel extends ObservableUIPojo {

	private HashMap<String, Object> dataModel = new HashMap<String, Object>();

	private static final String USER = "user";
	private static final String APPLICATION = "application";
	
	public void setProperty(String key, Object value) {
		dataModel.put(key, value);
	}
	
	public Object getProperty(String key) {
		return dataModel.get(key);
	}
	
	public void setUser(IUser user) {
		dataModel.put(USER, user);
	}

	public IUser getUser() {
		return (IUser) dataModel.get(USER);
	}

	public IApplication getApplication() {
		return (IApplication)dataModel.get(APPLICATION);
	}

	public void setApplication(IApplication application) {
		dataModel.put(APPLICATION, application);
	}

	public void importProject(File projectFolder, IProgressMonitor monitor) throws OpenshiftException, CoreException, InterruptedException {
		MavenProjectImportOperation mavenImport = new MavenProjectImportOperation(projectFolder);
		List<IProject> importedProjects = Collections.emptyList();
		if (mavenImport.isMavenProject()) {
			importedProjects = mavenImport.importToWorkspace(monitor);
		} else {
			importedProjects = new GeneralProjectImportOperation(projectFolder).importToWorkspace(monitor);
		}

		connectToGitRepo(importedProjects, monitor);

		createServerAdapterIfRequired();
	}

	private void connectToGitRepo(List<IProject> projects, IProgressMonitor monitor) throws CoreException {
		for (IProject project : projects) {
			connectToGitRepo(project, monitor);
		}
	}

	private void connectToGitRepo(IProject project, IProgressMonitor monitor) throws CoreException {
		new ConnectProviderOperation(project).execute(monitor);
	}

	public File cloneRepository(IProgressMonitor monitor) throws URISyntaxException, OpenshiftException,
			InvocationTargetException,
			InterruptedException {
		File destination = getDestinationDirectory(getApplication());
		cloneRepository(getApplication().getGitUri(), destination, monitor);
		return destination;
	}

	private void cloneRepository(String uri, File destination, IProgressMonitor monitor) throws URISyntaxException,
			OpenshiftException,
			InvocationTargetException,
			InterruptedException {
		if (destination.exists()) {
			FileUtil.completeDelete(destination);
		}
		URIish gitUri = new URIish(uri);
		CloneOperation cloneOperation =
				new CloneOperation(gitUri, true, null, destination, Constants.HEAD, "origin", 10 * 1024);
		cloneOperation.run(null);
		File gitDirectory = new File(destination, Constants.DOT_GIT);
		Activator.getDefault().getRepositoryUtil().addConfiguredRepository(gitDirectory);
	}

	private File getDestinationDirectory(IApplication application) {
		String applicationDirectory = "openshift-" + application.getName();
		// File workspace =
		// ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
		String userHome = System.getProperty("java.io.tmpdir");
		// File workDir = new File(workspace, applicationWorkingdir);
		return new File(userHome, applicationDirectory);
	}

	private void mergeWithRemote(Git git, String remoteName) throws CoreException, NoHeadException,
			ConcurrentRefUpdateException, CheckoutConflictException, InvalidMergeHeadsException,
			WrongRepositoryStateException, NoMessageException, IOException {
		Repository repository = git.getRepository();
		ObjectId objectId = repository.resolve("HEAD");
		git.merge().include(objectId).setStrategy(MergeStrategy.OURS).call();
	}

	private void createServerAdapterIfRequired() {
		// TODO
		Boolean b = (Boolean)getProperty(AdapterWizardPageModel.CREATE_SERVER);
		if( b != null && b.booleanValue() ) {
			IServerType type = (IServerType)getProperty(AdapterWizardPageModel.SERVER_TYPE);
			IRuntime rt = (IRuntime)getProperty(AdapterWizardPageModel.RUNTIME_DELEGATE);
			String mode = (String)getProperty(AdapterWizardPageModel.MODE);
			
			try {
				IServer server = ExpressServerUtils.createServer(rt, type, "Openshift Server1");
				ExpressServerUtils.fillServerWithOpenshiftDetails(server, getApplication().getApplicationUrl(), 
						getUser().getRhlogin(), getUser().getPassword(), 
						getUser().getDomain().getRhcDomain(), getApplication().getName(), mode);
			} catch(CoreException ce) {
				OpenshiftUIActivator.getDefault().getLog().log(ce.getStatus());
			} catch( OpenshiftException ose) {
				IStatus s = new Status(IStatus.ERROR, OpenshiftUIActivator.PLUGIN_ID, "Cannot create openshift server adapter", ose);
				OpenshiftUIActivator.getDefault().getLog().log(s);
			}
		}
	}

	private Git createGit(File repositoryFile) throws IOException {
		InitCommand init = Git.init();
		init.setDirectory(repositoryFile);
		init.setBare(false);
		return init.call();
	}

	private File createRepositoryFile(String name) {
		IPath workspace = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		IPath gitRepoProject = workspace.append(name);
		File repositoryFile = new File(gitRepoProject.toFile(), Constants.DOT_GIT);
		return repositoryFile;
	}

}
