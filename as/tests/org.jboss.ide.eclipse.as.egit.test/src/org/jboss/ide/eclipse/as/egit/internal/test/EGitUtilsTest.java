package org.jboss.ide.eclipse.as.egit.internal.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
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

	@Before
	public void setUp() throws Exception {
		Activator.getDefault().getRepositoryCache().clear();

		createMockSystemReader();

		this.testProject = new TestProject(true);

		this.gitDir = createGitDir(testProject);
		this.testRepository = new TestRepository(gitDir);
		setUserAndEmail(testRepository);
		testRepository.connect(testProject.getProject());

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
				"a.txt", "some text");
		addToRepository(file, testRepository);

		EGitUtils.commit(testProject.getProject(), null);

		testUtils.assertRepositoryContainsFiles(
				testRepository.getRepository(),
				new String[] { testUtils.getRepositoryPath(file) });
	}

	@Test
	public void fileAddedToCloneIsInOriginAfterPush() throws Exception {
		String fileName = "b.txt";
		String fileContent = "adietish@redhat.com";
		Repository clonedRepository = clonedTestRepository.getRepository();
		newRepositoryFile(fileName, fileContent, clonedRepository);
		Git git = new Git(clonedRepository);
		git.add().addFilepattern(fileName).call();
		git.commit().setCommitter(GIT_USER, GIT_EMAIL).setMessage("adding a new file").call();

		EGitUtils.push(clonedRepository, null);

		// does origin contain file added to clone?
		testUtils.assertRepositoryContainsFilesWithContent(
				clonedRepository,
				fileName,
				fileContent);
	}

	private void addToRepository(IFile file, TestRepository testRepository) throws IOException, CoreException {
		// List<IResource> resources = new ArrayList<IResource>();
		// resources.add(file);
		// new AddToIndexOperation(resources).execute(null);
		testRepository.track(new File(file.getLocation().toOSString()));
	}

	private static void newRepositoryFile(String name, String data, Repository repository) throws IOException {
		File file = new File(repository.getWorkTree(), name);
		write(file, data);
	}

	private static void write(final File file, final String body) throws IOException {
		FileUtils.mkdirs(file.getParentFile(), true);
		Writer w = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
		try {
			w.write(body);
		} finally {
			w.close();
		}
	}

}
