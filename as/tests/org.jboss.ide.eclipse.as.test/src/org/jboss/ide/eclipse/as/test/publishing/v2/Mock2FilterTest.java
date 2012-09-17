package org.jboss.ide.eclipse.as.test.publishing.v2;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.internal.ServerPreferences;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.jboss.ide.eclipse.as.core.server.IModulePathFilter;
import org.jboss.ide.eclipse.as.core.server.internal.DelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.test.util.IOUtil;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;

/**
 * Tests the concept of a behaviour having a filter... 
 * in this case, customized by the test case.
 * 
 * @author rob
 *
 */
public class Mock2FilterTest extends AbstractJSTDeploymentTester {
	public void testNormalLogic() throws CoreException, IOException, Exception {
		server = ServerRuntimeUtils.createMockServerWithRuntime(IJBossToolingConstants.SERVER_AS_60, 
				"name1", "default");
		server = ServerRuntimeUtils.useMock2PublishMethod(server);
		project = createProject();
		MockPublishMethod.reset();
		
		ServerPreferences.getInstance().setAutoPublishing(false);
		IModule mod = ServerUtil.getModule(project);
		server = ServerRuntimeUtils.addModule(server, mod);
		
		DelegatingServerBehavior beh = (DelegatingServerBehavior)server.loadAdapter(DelegatingServerBehavior.class, null);
		Mock2BehaviourDelegate del = (Mock2BehaviourDelegate)beh.getDelegate();
		del.setFilter(getSuffixFilter(null));
		
		ServerRuntimeUtils.publish(server);
		assertEquals(2, MockPublishMethod.getChanged().length);
		MockPublishMethod.reset();
		
		// Two already in the project
		// Note: One is application.xml which is touched by our module-restart crap on full publish
		ServerRuntimeUtils.publishFull(server);
		assertEquals(2, MockPublishMethod.getChanged().length);
		MockPublishMethod.reset();
		
		IFile textFile = project.getFile(getContentTextFilePath());
		IOUtil.setContents(textFile, 0);
		assertEquals(0, MockPublishMethod.getChanged().length);
		ServerRuntimeUtils.publishFull(server);
		assertEquals(3, MockPublishMethod.getChanged().length);
		MockPublishMethod.reset();
		
		del.setFilter(getSuffixFilter("txt"));
		ServerRuntimeUtils.publishFull(server);
		IPath[] changed;
		changed = MockPublishMethod.getChanged();
		assertEquals(2, changed.length);
		MockPublishMethod.reset();
		
		textFile = project.getFile(new Path(getContentDir()).append("other.txt"));
		IOUtil.setContents(textFile, 1);
		assertEquals(0, MockPublishMethod.getChanged().length);
		del.setFilter(getSuffixFilter(null));
		ServerRuntimeUtils.publishFull(server);
		assertEquals(4, MockPublishMethod.getChanged().length);
		MockPublishMethod.reset();

		
		del.setFilter(getSuffixFilter("txt"));
		ServerRuntimeUtils.publishFull(server);
		changed = MockPublishMethod.getChanged();
		assertEquals(2, changed.length);
		MockPublishMethod.reset();
	}
	
	protected IModulePathFilter getSuffixFilter(String suffix) {
		return new SuffixFilter(suffix);
	}
	
	protected class SuffixFilter implements IModulePathFilter {
		private String suffix; // filter OUT this suffix
		public SuffixFilter(String suffix) {
			this.suffix = suffix;
		}
		public boolean shouldInclude(IModuleResource moduleResource) {
			if( suffix == null )
				return true;
			return !moduleResource.getName().endsWith(suffix);
		}
		// TODO
		public IModuleResource[] getFilteredMembers() throws CoreException {
			return null;
		}
	}
	
	protected void assertRemoved(boolean isAs7, String[] nonAs7, String[] as7) {
		assertExpectedArtifacts(isAs7, nonAs7, as7, MockPublishMethod.getRemoved());
	}

	protected void assertChanged(boolean isAs7, String[] nonAs7, String[] as7) {
		assertExpectedArtifacts(isAs7, nonAs7, as7, MockPublishMethod.getChanged());
	}

	protected void assertExpectedArtifacts(boolean isAs7, String[] nonAs7, String[] as7, IPath[] artifacts) {
		if (isAs7) {
			assertEquals(as7.length, artifacts.length);
		} else {
			assertEquals(nonAs7.length, artifacts.length);
		}

		if (isAs7) {
			for (String expectedPath : as7) {
				if (contains(MockPublishMethod.MOCK_ROOT + "/" + expectedPath, artifacts)) {
					continue;
				}
				fail(expectedPath + " was not among the changed/removed artifacts");
			}
		} else {
			for (String expectedPath : nonAs7) {
				if (contains(MockPublishMethod.MOCK_ROOT + "/" + expectedPath, artifacts)) {
					continue;
				}
				fail(expectedPath + " was not among the changed/removed artifacts");
			}
		}
	}

	protected boolean contains(String expectedPath, IPath[] paths) {
		for (IPath path : paths) {
			if (expectedPath.equals(path.toString())) {
				return true;
			}
		}
		return false;
	}

	
}
