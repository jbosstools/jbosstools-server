package org.jboss.tools.as.test.core.parametized.server;

import java.util.Collection;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * This class will test properties of a default created server and runtime 
 * for properties that should never be null.
 * 
 * @author rob
 *
 */
@RunWith(value = Parameterized.class)
public class CreateRuntimeTwiceTest extends TestCase {
	private String serverType;
	private IServer server;
	@Parameters
	public static Collection<Object[]> data() {
		 return ServerParameterUtils.asCollection(ServerParameterUtils.getAllJBossServerTypeParamterers());
	}
	 
	public CreateRuntimeTwiceTest(String serverType) {
		this.serverType = serverType;
	}
	
	@Before
	public void setUp() {
		server = ServerCreationTestUtils.createMockServerWithRuntime(serverType, getClass().getName() + serverType);
	}

	@After
	public void tearDown() throws Exception {
		ServerCreationTestUtils.deleteAllServersAndRuntimes();
	}
	
	@Test
	public void createRuntimes() throws CoreException {
		IServerType type = ServerCore.findServerType(serverType);
		IRuntimeType runtimeType = type.getRuntimeType();
		
		IRuntimeWorkingCopy firstRuntime = runtimeType.createRuntime(null, new NullProgressMonitor());
		IRuntime savedRuntime = firstRuntime.save(true, new NullProgressMonitor());
		
		IRuntimeWorkingCopy secondRuntime = runtimeType.createRuntime(null, new NullProgressMonitor());
		IRuntime secondSavedRuntime = secondRuntime.save(true, new NullProgressMonitor());
		
		assertNotSame(savedRuntime.getName(), secondSavedRuntime.getName());
		assertNotSame(savedRuntime, secondSavedRuntime);				
		assertFalse("Why are two different runtimes " + runtimeType.getId() + " created with the same ID ?!", savedRuntime.getId().equals(secondSavedRuntime.getId()));
	}
}
