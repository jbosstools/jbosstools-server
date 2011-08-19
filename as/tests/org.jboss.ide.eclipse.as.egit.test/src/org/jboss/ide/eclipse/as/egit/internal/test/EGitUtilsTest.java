package org.jboss.ide.eclipse.as.egit.internal.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.egit.core.Activator;
import org.eclipse.egit.core.RepositoryCache;
import org.eclipse.egit.core.op.CloneOperation;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.util.FileUtils;
import org.eclipse.jgit.util.SystemReader;
import org.jboss.ide.eclipse.as.egit.core.EGitUtils;
import org.jboss.ide.eclipse.as.egit.internal.test.util.MockSystemReader;
import org.jboss.ide.eclipse.as.egit.internal.test.util.TestProject;
import org.jboss.ide.eclipse.as.egit.internal.test.util.TestRepository;
import org.jboss.ide.eclipse.as.egit.internal.test.util.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EGitUtilsTest {

	private static final String GIT_EMAIL = "dummyUser@redhat.com";

	private static final String GIT_USER = "dummyUser";

	protected final TestUtils testUtils = new TestUtils();

	private File gitDir;
	private TestRepository testRepository;
	private TestProject testProject;
	private TestRepository clonedTestRepository;

	private IFile testFile;

	@Before
	public void setUp() throws Exception {
		Activator.getDefault().getRepositoryCache().clear();

		createMockSystemReader();

		this.testProject = new TestProject(true);
		this.testFile = testUtils.addFileToProject(testProject.getProject(), "a.txt", "1234");

		this.gitDir = createGitDir(testProject);
		this.testRepository = new TestRepository(gitDir);
		setUserAndEmail(testRepository);
		testRepository.connect(testProject.getProject());
		testRepository.addAndCommit(testProject.getProject(), testFile.getLocation().toFile(), "commit");
		
		this.clonedTestRepository = cloneRepository(testRepository.getRepository());
	}

	private void setUserAndEmail(TestRepository testRepository) {
		StoredConfig config = testRepository.getRepository().getConfig();
		config.setString(ConfigConstants.CONFIG_USER_SECTION, null, ConfigConstants.CONFIG_KEY_NAME,
				GIT_USER);
		config.setString(ConfigConstants.CONFIG_USER_SECTION, null, ConfigConstants.CONFIG_KEY_EMAIL,
				GIT_EMAIL);
	}

	private TestRepository cloneRepository(Repository repository) throws URISyntaxException, InvocationTargetException,
			InterruptedException, IOException {
		URIish uri = new URIish("file:///" + repository.getDirectory().toString());
		File workspaceDir = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
		File clonedRepositoryFile =
				new File(workspaceDir, "clonedRepository-" + String.valueOf(System.currentTimeMillis()));
		CloneOperation clop =
				new CloneOperation(uri, true, null, clonedRepositoryFile, Constants.R_HEADS + Constants.MASTER,
						Constants.DEFAULT_REMOTE_NAME, 0);
		clop.run(null);
		RepositoryCache repositoryCache = Activator.getDefault().getRepositoryCache();
		Repository clonedRepository = repositoryCache
				.lookupRepository(new File(clonedRepositoryFile, Constants.DOT_GIT));
		TestRepository testRepository = new TestRepository(clonedRepository);
		// Repository clonedRepository = new FileRepository(new
		// File(clonedRepositoryFile, Constants.DOT_GIT));
		// TestRepository testRepository = new TestRepository(clonedRepository);
		// we push to branch "test" of repository2
		// RefUpdate createBranch = testRepository.getRepository().updateRef(
		// "refs/heads/test");
		// createBranch.setNewObjectId(testRepository.getRepository().resolve(
		// "refs/heads/master"));
		// createBranch.update();

		return testRepository;
	}

	private void createMockSystemReader() {
		MockSystemReader mockSystemReader = new MockSystemReader();
		SystemReader.setInstance(mockSystemReader);
		File workspaceFile = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile()
				.getAbsoluteFile();
		mockSystemReader.setProperty(Constants.GIT_CEILING_DIRECTORIES_KEY,
				workspaceFile.toString());
	}

	private File createGitDir(TestProject testProject) throws IOException {
		return new File(testProject.getProject().getLocation().toFile(), Constants.DOT_GIT);
	}

	@After
	public void tearDown() throws Exception {
		cleanupRepository(testRepository);
		cleanupRepository(clonedTestRepository);

		testProject.dispose();
		Activator.getDefault().getRepositoryCache().clear();
	}

	private static void cleanupRepository(TestRepository testRepository) throws IOException {
		File repositoryDirectory = testRepository.getRepository().getDirectory();
		File repositoryParent = repositoryDirectory.getParentFile();
		if (repositoryParent.exists()) {
			FileUtils.delete(repositoryParent, FileUtils.RECURSIVE | FileUtils.RETRY);
		}
		testRepository.dispose();
	}

	@Test
	public void canCommitFileInProject() throws Exception {
		IFile file = testUtils.addFileToProject(
				testProject.getProject(),
				"b.txt", "some text");
		addToRepository(file, testRepository);

		EGitUtils.commit(testProject.getProject(), null);

		testUtils.assertRepositoryContainsFiles(testRepository.getRepository(),
				new String[] { testUtils.getRepositoryPath(file), testUtils.getRepositoryPath(testFile) });
	}

	@Test
	public void canPushRepoToAntoherRepo() throws Exception {

		String clonedFilePath = testUtils.getRepositoryPath(testFile);
		File clonedFile = new File(clonedFilePath);
		FileWriter writer = new FileWriter(clonedFile);
		writer.write("4321");
		writer.close();

		new Git(clonedTestRepository.getRepository())
				.commit()
				.setCommitter(GIT_USER, GIT_EMAIL)
				.setMessage("commit")
				.call();

		EGitUtils.push(clonedTestRepository.getRepository(), null);

		testUtils.assertRepositoryContainsFilesWithContent(
				testRepository.getRepository(),
				testUtils.getRepositoryPath(testFile),
				"4321");
		}

	private void addToRepository(IFile file, TestRepository testRepository) throws IOException, CoreException {
		// List<IResource> resources = new ArrayList<IResource>();
		// resources.add(file);
		// new AddToIndexOperation(resources).execute(null);
		testRepository.track(new File(file.getLocation().toOSString()));
	}
}
