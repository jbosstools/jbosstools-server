package org.jboss.ide.eclipse.as.egit.internal.test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.egit.core.Activator;
import org.jboss.ide.eclipse.as.egit.core.EGitUtils;
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

	private TestRepository testRepository;
	private TestProject testProject;
	private TestRepository clonedTestRepository;

	@Before
	public void setUp() throws Exception {
		Activator.getDefault().getRepositoryCache().clear();

		this.testProject = new TestProject(true);

		this.testRepository = new TestRepository(TestUtils.createGitDir(testProject));
		testRepository.createMockSystemReader(ResourcesPlugin.getWorkspace().getRoot().getLocation());
		testRepository.setUserAndEmail(GIT_USER, GIT_EMAIL);
		testRepository.connect(testProject.getProject());

		this.clonedTestRepository = cloneRepository(testRepository);
	}

	private TestRepository cloneRepository(TestRepository repository) throws URISyntaxException,
			InvocationTargetException, InterruptedException, IOException {
		File workspaceDir = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
		File clonedRepositoryFile =
				new File(workspaceDir, "clonedRepository-" + String.valueOf(System.currentTimeMillis()));
		return testRepository.cloneRepository(clonedRepositoryFile);
	}

	@After
	public void tearDown() throws Exception {
		testRepository.dispose();
		clonedTestRepository.dispose();

		testProject.dispose();
		Activator.getDefault().getRepositoryCache().clear();
	}

	@Test
	public void canCommitFileInProject() throws Exception {
		String fileName = "a.txt";
		String fileContent = "adietish@redhat.com";

		IFile file = testUtils.addFileToProject(
				testProject.getProject(),
				fileName,
				fileContent);
		testRepository.track(file);

		EGitUtils.commit(testProject.getProject(), null);

		testUtils.assertRepositoryContainsFilesWithContent(
				testRepository.getRepository(),
				new String[] { testUtils.getPathInRepository(file), fileContent });
	}

	@Test
	public void fileAddedToCloneIsInOriginAfterPush() throws Exception {
		String fileName = "b.txt";
		String fileContent = "adietish@redhat.com";

		File file = clonedTestRepository.createFile(fileName, fileContent);
		clonedTestRepository.addAndCommit(file, "adding a file");

		EGitUtils.push(clonedTestRepository.getRepository(), null);

		// does origin contain file added to clone?
		testUtils.assertRepositoryContainsFilesWithContent(
				clonedTestRepository.getRepository(),
				fileName,
				fileContent);
	}

	@Test
	public void fileAddedToCloneIsInRemoteAfterPush() throws Exception {
		TestProject testProject2 = null;
		TestRepository testRepository2 = null;
		String fileName = "c.txt";
		String fileContent = "adietish@redhat.com";
		String remoteRepoName = "openshift";
		
		try {
			testProject2 = new TestProject(true);
			File gitDir = TestUtils.createGitDir(testProject2);
			testRepository2 = new TestRepository(gitDir);
			clonedTestRepository.addRemoteTo(remoteRepoName, testRepository2.getRepository());

			File file = clonedTestRepository.createFile(fileName, fileContent);
			clonedTestRepository.addAndCommit(file, "adding a file");

			EGitUtils.push(remoteRepoName, clonedTestRepository.getRepository(), null);

			// does origin contain file added to clone?
			testUtils.assertRepositoryContainsFilesWithContent(
					clonedTestRepository.getRepository(),
					fileName,
					fileContent);
		} finally {
			if (testProject2 != null) {
				testProject2.dispose();
			}
			if (testRepository2 != null) {
				testRepository2.dispose();
			}
		}
	}
}
