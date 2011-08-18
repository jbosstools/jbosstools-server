package org.jboss.ide.eclipse.as.egit.internal.test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.egit.core.Activator;
import org.eclipse.egit.core.RepositoryCache;
import org.eclipse.egit.core.op.AddToIndexOperation;
import org.eclipse.egit.core.op.CloneOperation;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.util.FileUtils;
import org.eclipse.jgit.util.SystemReader;
import org.jboss.ide.eclipse.as.egit.core.EgitUtils;
import org.jboss.ide.eclipse.as.egit.internal.test.util.MockSystemReader;
import org.jboss.ide.eclipse.as.egit.internal.test.util.TestProject;
import org.jboss.ide.eclipse.as.egit.internal.test.util.TestRepository;
import org.jboss.ide.eclipse.as.egit.internal.test.util.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EGitUtilsTest {

	protected final TestUtils testUtils = new TestUtils();
	private List<IResource> resources = new ArrayList<IResource>();

	private File gitDir;
	private TestRepository testRepository;
	private Repository repository;
	private TestProject testProject;
	private IProject project;
	private TestRepository clonedTestRepository;

	@Before
	public void setUp() throws Exception {
		Activator.getDefault().getRepositoryCache().clear();

		this.testProject = new TestProject(true);
		this.project = testProject.getProject();

		createMockSystemReader();

		this.gitDir = createGitDir(testProject);
		this.testRepository = new TestRepository(gitDir);
		this.repository = testRepository.getRepository();
		testRepository.connect(project);

		this.clonedTestRepository = cloneRepository(repository);
	}

	private TestRepository cloneRepository(Repository repository) throws URISyntaxException, InvocationTargetException,
			InterruptedException, IOException {
		URIish uri = new URIish("file:///" + repository.getDirectory().toString());
		String repoParent = repository.getDirectory().getParent();
		File clonedRepositoryFile =
				new File(repoParent, "clonedRepository-" + String.valueOf(System.currentTimeMillis()));
		CloneOperation clop =
				new CloneOperation(uri, true, null, clonedRepositoryFile, "refs/heads/master", "origin", 0);
		clop.run(null);
		RepositoryCache repositoryCache = Activator.getDefault().getRepositoryCache();
		Repository clonedRepository =
				repositoryCache.lookupRepository(new File(clonedRepositoryFile, Constants.DOT_GIT));
		return new TestRepository(clonedRepository);
	}

	private void createMockSystemReader() {
		MockSystemReader mockSystemReader = new MockSystemReader();
		SystemReader.setInstance(mockSystemReader);
		mockSystemReader.setProperty(Constants.GIT_CEILING_DIRECTORIES_KEY,
				ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile()
						.getAbsoluteFile().toString());
	}

	private File createGitDir(TestProject testProject) throws IOException {
		IPath workspaceRoot = project.getWorkspace().getRoot().getRawLocation();
		// IPath randomFolder =
		// workspaceRoot.append(String.valueOf(System.currentTimeMillis()));
		// File gitDir = new File(randomFolder.toFile(), Constants.DOT_GIT);
		File gitDir = new File(workspaceRoot.toFile(), Constants.DOT_GIT);
		if (gitDir.exists()) {
			// guard
			FileUtils.delete(gitDir, FileUtils.RECURSIVE | FileUtils.RETRY);
		}
		return gitDir;
	}

	@After
	public void tearDown() throws Exception {
		cleanupRepository(testRepository);
		cleanupRepository(clonedTestRepository);
		
		testProject.dispose();
		project = null;
		Activator.getDefault().getRepositoryCache().clear();
	}

	private static void cleanupRepository(TestRepository testRepository) throws IOException {
		File repositoryDirectory = testRepository.getRepository().getDirectory();
		if (repositoryDirectory.exists()) {
			FileUtils.delete(repositoryDirectory, FileUtils.RECURSIVE | FileUtils.RETRY);
		}
		testRepository.dispose();
	}

	@Test
	public void canCommitFileInProject() throws Exception {
		IFile file = testUtils.addFileToProject(
				testProject.getProject(),
				"a.txt", "some text");
		resources.add(file);
		new AddToIndexOperation(resources).execute(null);

		EgitUtils.commit(project, null);

		testUtils.assertRepositoryContainsFiles(repository, new String[] { testUtils.getRepositoryPath(file) });
	}

	@Test
	public void canPushRepoToAntoherRepo() throws Exception {
		IFile file = testUtils.addFileToProject(testProject.getProject(), "a.txt", "some text");
		resources.add(file);
		new AddToIndexOperation(resources).execute(null);
		EgitUtils.commit(project, null);

		EgitUtils.push(clonedTestRepository.getRepository(), null);

		testUtils.assertRepositoryContainsFiles(repository, new String[] { testUtils.getRepositoryPath(file) });
	}

}
