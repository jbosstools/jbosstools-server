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

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.egit.core.op.CloneOperation;
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
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.tools.common.ui.databinding.ObservableUIPojo;
import org.jboss.tools.openshift.express.client.IApplication;
import org.jboss.tools.openshift.express.client.IUser;
import org.jboss.tools.openshift.express.client.OpenshiftException;

/**
 * @author André Dietisheim
 */
public class ServerAdapterWizardModel extends ObservableUIPojo {

	private static final String REMOTE_NAME = "openshift";
	private IUser user;
	private IApplication application;
	
	public void setUser(IUser user) {
		this.user = user;
	}
	
	public IUser getUser() {
		return user;
	}

	public IApplication getApplication() {
		return application;
	}

	public void setApplication(IApplication application) {
		this.application = application;
	}

	public void setupProject() throws OpenshiftException, URISyntaxException, InvocationTargetException, InterruptedException, IOException, NoHeadException, ConcurrentRefUpdateException, CheckoutConflictException, InvalidMergeHeadsException, WrongRepositoryStateException, NoMessageException, CoreException {
		String applicationWorkingdir = "openshift-" + application.getName();
//		File workspace = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
		String userHome = System.getProperty("java.io.tmpdir");
//		File workDir = new File(workspace, applicationWorkingdir);
		File clonedDirectory = new File(userHome, applicationWorkingdir);
		URIish gitUri = new URIish(application.getGitUri());
		if (clonedDirectory.exists()) {
			FileUtil.completeDelete(clonedDirectory);
		} 
		CloneOperation cloneOperation = new CloneOperation(gitUri, true, null, clonedDirectory, Constants.HEAD, "origin", 10 * 1024);
//		cloneOperation.setCredentialsProvider(new UsernamePasswordCredentialsProvider(user.getRhlogin(), user.getPassword()));
		cloneOperation.run(null);
		File gitDirectory = new File(clonedDirectory, Constants.DOT_GIT);
		Activator.getDefault().getRepositoryUtil().addConfiguredRepository(gitDirectory);
//		File repositoryFile = createRepositoryFile(applicationWorkingdir);
//		Git git = createGit(repositoryFile);
		// TODO replace remote name by user setting
//		Repository repository = git.getRepository();
//		EGitUtils.addRemoteTo(REMOTE_NAME, new URIish(application.getGitUri()), repository);
//		mergeWithRemote(git, REMOTE_NAME);
				
		createServerAdapterIfRequired();
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
