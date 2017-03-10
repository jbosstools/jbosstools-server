package org.jboss.tools.as.ui.bot.itests.parametized.server;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.Path;
import org.jboss.reddeer.common.wait.WaitWhile;
import org.jboss.reddeer.core.condition.JobIsRunning;
import org.jboss.reddeer.junit.internal.runner.ParameterizedRequirementsRunnerFactory;
import org.jboss.reddeer.junit.runner.RedDeerSuite;
import org.jboss.reddeer.workbench.ui.dialogs.WorkbenchPreferenceDialog;
import org.jboss.tools.as.ui.bot.itests.Activator;
import org.jboss.tools.as.ui.bot.itests.parametized.CleanEnvironmentUtils;
import org.jboss.tools.as.ui.bot.itests.parametized.MatrixUtils;
import org.jboss.tools.as.ui.bot.itests.reddeer.ui.SearchingForRuntimesDialog;
import org.jboss.tools.as.ui.bot.itests.reddeer.util.RuntimeDetectionUtility;
import org.jboss.tools.as.ui.bot.itests.reddeer.util.DisableSecureStorageRequirement.DisableSecureStorage;
import org.jboss.tools.common.util.FileUtil;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Parameterized.UseParametersRunnerFactory;

import junit.framework.TestCase;

@RunWith(RedDeerSuite.class)
@UseParametersRunnerFactory(ParameterizedRequirementsRunnerFactory.class)
@DisableSecureStorage
public class RuntimeDetectionDuplicatesTest extends TestCase {

    @Parameters
    public static Collection<Object[]> data(){
    	return MatrixUtils.toMatrix(new Object[][]{PomServerConstants.getJBossHomeFlags()});
    }
    

    private String serverHomeFlag;
    public RuntimeDetectionDuplicatesTest(String serverHome) {
    	this.serverHomeFlag = serverHome;
    }
    

	@Test
	public void duplicateRuntimes(){
		
		assertNotNull(serverHomeFlag);
		System.out.println("flag: " + serverHomeFlag);
		String home = System.getProperty(serverHomeFlag);
		System.out.println("sysprop has value: " + home);
		try {
			Thread.sleep(3500);
		} catch(InterruptedException ie) {
			
		}
		assertNotNull(home);
		assertTrue(new File(home).exists());
		assertTrue(new File(home).isDirectory());
		
		
		// So we have a valid folder here. Let's make 2 copies
		
		
		File tmpDir = Activator.getStateFolder().append("RuntimeDetectionDuplicatesTest").toFile();
		tmpDir.mkdirs();
		File tmpServerPath = new File(tmpDir, "tmpServerCopy_" + System.currentTimeMillis());
		
		String suffix = new Path(home).lastSegment();
		File tmpServerAPath = new File(tmpServerPath, "serverA/" + suffix);
		File tmpServerBPath = new File(tmpServerPath, "serverB/" + suffix);
		
		FileUtil.copyDir(new File(home), tmpServerAPath, true, true, true);
		FileUtil.copyDir(new File(home), tmpServerBPath, true, true, true);
		
		SearchingForRuntimesDialog searchingForRuntimesDialog = RuntimeDetectionUtility.addPath(tmpServerPath.getAbsolutePath());
		assertFoundRuntimesNumber(searchingForRuntimesDialog, 2);
		
		searchingForRuntimesDialog = RuntimeDetectionUtility.searchFirstPath();
		searchingForRuntimesDialog.ok();
		
		new WaitWhile(new JobIsRunning());
		searchingForRuntimesDialog = RuntimeDetectionUtility.searchFirstPath();
		assertFoundRuntimesNumber(searchingForRuntimesDialog, 2);
		
		searchingForRuntimesDialog = RuntimeDetectionUtility.searchFirstPath();
		searchingForRuntimesDialog.hideAlreadyCreatedRuntimes();
		assertFoundRuntimesNumber(searchingForRuntimesDialog, 0);
		
		new WorkbenchPreferenceDialog().ok();
	}

	@After
	public void deleteServers() throws IOException{
		CleanEnvironmentUtils.cleanAll();
	}
	
	private void assertFoundRuntimesNumber(SearchingForRuntimesDialog dialog, int expected) {
		List<org.jboss.tools.as.ui.bot.itests.reddeer.Runtime> runtimes = dialog.getRuntimes();
		dialog.cancel();
		assertThat(runtimes.size(), is(expected));
	}
}
