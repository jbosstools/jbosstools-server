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
package org.jboss.ide.eclipse.as.egit.core;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.egit.core.op.CommitOperation;
import org.eclipse.egit.core.op.PushOperation;
import org.eclipse.egit.core.op.PushOperationSpecification;
import org.eclipse.egit.core.project.RepositoryMapping;
import org.eclipse.jgit.errors.NotSupportedException;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.UserConfig;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.osgi.util.NLS;
import org.jboss.ide.eclipse.as.egit.core.internal.EGitCoreActivator;

/**
 * The Class EgitUtils.
 * 
 * @author André Dietisheim
 */
public class EgitUtils {

	private static final RefSpec DEFAULT_PUSH_REF_SPEC =
			new RefSpec("refs/heads/*:refs/heads/*"); //$NON-NLS-1$

	private static final int PUSH_TIMEOUT = 10 * 1024;

	/**
	 * Commits the given project to it's configured repository.
	 * 
	 * @param project
	 *            the project
	 * @param monitor
	 *            the monitor
	 * @throws CoreException
	 *             the core exception
	 */
	public static void commit(IProject project, IProgressMonitor monitor) throws CoreException {
		/**
		 * TODO: add capability to commit selectively
		 */
		Repository repository = getRepository(project);
		UserConfig userConfig = getUserConfig(repository);
		CommitOperation op = new CommitOperation(
				null,
				null,
				null,
				getSubject(userConfig.getAuthorName(), userConfig.getAuthorEmail()),
				getSubject(userConfig.getCommitterName(), userConfig.getCommitterEmail()),
				"Initial commit");
		op.setCommitAll(true);
		op.setRepository(repository);
		op.execute(monitor);
	}

	/**
	 * Pushes the given repository to it's configured remote.
	 * 
	 * @param repository
	 *            the source repository
	 * @param monitor
	 *            the monitor
	 * @throws CoreException
	 *             the core exception
	 */
	public static void push(Repository repository, IProgressMonitor monitor)
			throws CoreException {
		try {
			RemoteConfig remoteConfig = getRemoteConfig(repository);
			if (remoteConfig == null) {
				IStatus status = new Status(IStatus.ERROR, EGitCoreActivator.PLUGIN_ID, NLS.bind(
						"Repository \"{0}\" has no remote repository configured", repository.toString()));
				throw new CoreException(status);
			}
			PushOperation pop = createPushOperation(repository, remoteConfig);
			pop.run(monitor);
		} catch (CoreException e) {
			throw e;
		} catch (Exception e) {
			IStatus status = new Status(IStatus.ERROR, EGitCoreActivator.PLUGIN_ID,
					NLS.bind("Could not push repo {0}", repository.toString()), e);
			throw new CoreException(status);
		}
	}

	private static PushOperation createPushOperation(String remoteName, Repository repository) {
		return new PushOperation(repository, remoteName, false, PUSH_TIMEOUT);
	}

	private static PushOperation createPushOperation(Repository repository, RemoteConfig remoteConfig)
			throws CoreException {

		PushOperationSpecification spec = new PushOperationSpecification();
		List<URIish> urisToPush = getPushURIs(remoteConfig);
		List<RefSpec> pushRefSpecs = getPushRefSpecs(remoteConfig);
		addURIRefToPushSpecification(urisToPush, pushRefSpecs, repository, spec);

		return new PushOperation(repository, spec, false, PUSH_TIMEOUT);
	}

	/**
	 * Adds the given push uris to the given push operation specification.
	 * 
	 * @param urisToPush
	 *            the uris to push
	 * @param pushRefSpecs
	 *            the push ref specs
	 * @param repository
	 *            the repository
	 * @param spec
	 *            the spec
	 * @throws CoreException
	 *             the core exception
	 */
	private static void addURIRefToPushSpecification(List<URIish> urisToPush, List<RefSpec> pushRefSpecs,
			Repository repository, PushOperationSpecification spec) throws CoreException {
		for (URIish uri : urisToPush) {
			try {
				spec.addURIRefUpdates(uri,
						Transport.open(repository, uri).findRemoteRefUpdatesFor(pushRefSpecs));
			} catch (NotSupportedException e) {
				IStatus status =
						new Status(IStatus.ERROR, EGitCoreActivator.PLUGIN_ID, NLS.bind(
								"Could not connect repository \"{0}\" to a remote", repository.toString()), e);
				throw new CoreException(status);
			} catch (IOException e) {
				IStatus status = new Status(IStatus.ERROR, EGitCoreActivator.PLUGIN_ID, NLS.bind(
						"Could not convert remote specifications for repository \"{0}\" to a remote",
						repository.toString()), e);
				throw new CoreException(status);
			}
		}
	}

	/**
	 * Gets the push uris from the given remoteConfig.
	 * 
	 * @param remoteConfig
	 *            the remote config
	 * @return the push ur is
	 */
	private static List<URIish> getPushURIs(RemoteConfig remoteConfig) {
		List<URIish> urisToPush = new ArrayList<URIish>();
		for (URIish uri : remoteConfig.getPushURIs())
			urisToPush.add(uri);
		if (urisToPush.isEmpty() && !remoteConfig.getURIs().isEmpty())
			urisToPush.add(remoteConfig.getURIs().get(0));
		return urisToPush;
	}

	/**
	 * Gets the push RefSpecs from the given remote configuration.
	 * 
	 * @param config
	 *            the config
	 * @return the push ref specs
	 */
	private static List<RefSpec> getPushRefSpecs(RemoteConfig config) {
		List<RefSpec> pushRefSpecs = new ArrayList<RefSpec>();
		pushRefSpecs.addAll(config.getPushRefSpecs());
		if (pushRefSpecs.isEmpty()) {
			// default push to all branches
			pushRefSpecs.add(DEFAULT_PUSH_REF_SPEC);
		}
		return pushRefSpecs;
	}

	/**
	 * Gets the repository that is configured to the given project.
	 * 
	 * @param project
	 *            the project
	 * @return the repository
	 */
	private static Repository getRepository(IProject project) {
		RepositoryMapping repositoryMapping = RepositoryMapping.getMapping(project);
		if (repositoryMapping == null) {
			return null;
		}
		return repositoryMapping.getRepository();
	}

	/**
	 * Gets the UserConfig from the given repository
	 * 
	 * @param repository
	 *            the repository
	 * @return the user config
	 */
	private static UserConfig getUserConfig(Repository repository) {
		return repository.getConfig().get(UserConfig.KEY);
	}

	private static String getSubject(String name, String email) {
		return new StringBuilder().append(name).append(" <").append(email).append('>').toString();
	}

	/**
	 * Returns the configuration of the remote repository that is set to the
	 * given repository. Returns
	 * <code>null</null> if none was configured or if there's no remote repo configured.
	 * 
	 * @param repository
	 *            the repository to get the remote repo configuration from
	 * @return the configurtion of the remote repository
	 * @throws CoreException
	 *             the core exception
	 */
	public static RemoteConfig getRemoteConfig(Repository repository) throws CoreException {
		String branch = null;
		try {
			branch = repository.getBranch();
		} catch (IOException e) {
			IStatus status = new Status(IStatus.ERROR, EGitCoreActivator.PLUGIN_ID,
					NLS.bind("Could not get branch on repository \"{0}\"", repository.toString()), e);
			throw new CoreException(status);
		}

		String remoteName = getRemoteName(repository, branch);
		List<RemoteConfig> allRemotes = getRemoteConfigs(repository);
		return getRemoteConfig(remoteName, allRemotes);
	}

	/**
	 * Gets the remote config with the given name from the list of remote
	 * repositories.
	 * 
	 * @param remoteName
	 *            the remote name
	 * @param remoteRepositories
	 *            the remote repositories
	 * @return the remote config
	 */
	private static RemoteConfig getRemoteConfig(String remoteName, List<RemoteConfig> remoteRepositories) {
		RemoteConfig defaultConfig = null;
		RemoteConfig configuredConfig = null;
		for (RemoteConfig config : remoteRepositories) {
			if (config.getName().equals(Constants.DEFAULT_REMOTE_NAME))
				defaultConfig = config;
			if (remoteName != null && config.getName().equals(remoteName))
				configuredConfig = config;
		}

		if (configuredConfig == null) {
			return defaultConfig;
		}
		return configuredConfig;
	}

	/**
	 * Gets the remote configs from the given repository.
	 * 
	 * @param repository
	 *            the repository
	 * @return the remote configs
	 */
	private static List<RemoteConfig> getRemoteConfigs(Repository repository) {
		List<RemoteConfig> remoteConfigs = new ArrayList<RemoteConfig>();
		try {
			remoteConfigs =
					RemoteConfig.getAllRemoteConfigs(repository.getConfig());
		} catch (URISyntaxException e) {
			remoteConfigs = new ArrayList<RemoteConfig>();
		}
		return remoteConfigs;
	}

	/**
	 * Gets the name of the remote repository for a given repository and branch.
	 * 
	 * @param repository
	 *            the repository
	 * @param branch
	 *            the branch
	 * @return the remote name
	 */
	private static String getRemoteName(Repository repository, String branch) {
		String remoteName = null;
		if (ObjectId.isId(branch)) {
			remoteName = Constants.DEFAULT_REMOTE_NAME;
		} else {
			remoteName = repository.getConfig().getString(
					ConfigConstants.CONFIG_BRANCH_SECTION, branch,
					ConfigConstants.CONFIG_REMOTE_SECTION);
		}
		return remoteName;
	}
}
