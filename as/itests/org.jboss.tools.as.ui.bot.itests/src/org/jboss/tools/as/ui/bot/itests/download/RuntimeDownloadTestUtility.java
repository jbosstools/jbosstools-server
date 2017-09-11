package org.jboss.tools.as.ui.bot.itests.download;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.eclipse.reddeer.common.condition.AbstractWaitCondition;
import org.eclipse.reddeer.common.exception.WaitTimeoutExpiredException;
import org.eclipse.reddeer.common.logging.Logger;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.eclipse.reddeer.swt.condition.ShellIsAvailable;
import org.eclipse.reddeer.jface.wizard.WizardDialog;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.eclipse.reddeer.workbench.ui.dialogs.WorkbenchPreferenceDialog;
import org.jboss.tools.as.ui.bot.itests.parametized.CleanEnvironmentUtils;
import org.jboss.tools.as.ui.bot.itests.reddeer.ui.RuntimeDetectionPreferencePage;
import org.jboss.tools.as.ui.bot.itests.reddeer.util.FileUtils;
import org.jboss.tools.as.ui.bot.itests.reddeer.util.RuntimeDetectionUtility;
import org.jboss.tools.runtime.reddeer.wizard.TaskWizardFirstPage;
import org.jboss.tools.runtime.reddeer.wizard.TaskWizardLoginPage;
import org.jboss.tools.runtime.reddeer.wizard.TaskWizardSecondPage;
import org.jboss.tools.runtime.reddeer.wizard.TaskWizardThirdPage;

/**
 * RuntimeDownloadTestBase is base class for testing download of runtimes.
 * 
 * It provides methods to process runtime downloading.
 * 
 * @author Radoslav Rabara
 * @author Petr Suchy
 *
 */
public final class RuntimeDownloadTestUtility extends RuntimeDetectionUtility {
	private static final String JBOSS_ORG_USERNAME_PROPERTY_KEY = "jboss.org.username";
	private static final String JBOSS_ORG_PASSWORD_PROPERTY_KEY = "jboss.org.password";
	
	// UI elements
	protected WizardDialog runtimeDownloadWizard;
	private Logger log = new Logger(RuntimeDownloadTestUtility.class);

	
	private String username = System.getProperty(JBOSS_ORG_USERNAME_PROPERTY_KEY);
	private String password = System.getProperty(JBOSS_ORG_PASSWORD_PROPERTY_KEY);

	private File tmpPath;

	
	public RuntimeDownloadTestUtility() {
		this(getDefaultDownloadPath());
	}
	public RuntimeDownloadTestUtility(File f) {
		tmpPath = f;
		if( !tmpPath.exists())
			tmpPath.mkdir();
	}
	
	protected static File getDefaultDownloadPath() {
		File tmpDir = new File(System.getProperty("java.io.tmpdir"));
		File f = new File(tmpDir, "tmpServer_" + System.currentTimeMillis());
		return f;
	}

	public void setCredentials(String user, String pass) {
		this.username = user;
		this.password = pass;
	}
	
	/*
	 * Allow subclasses to override this and set a path for persistence
	 */
	public void setPath(File path) {
		if( tmpPath.exists()) {
			try {
				FileUtils.deleteDirectory(tmpPath);
			} catch(IOException ioe) {
				log.debug(ioe.getMessage());
			}
		}
		this.tmpPath = path;
	}
	
	public void clean() {
		clean(true);
	}
	
	public void clean(boolean delete) {
		if( delete )
			new WaitUntil(new SuccesfullyDeleted(tmpPath));
		CleanEnvironmentUtils.cleanAll();
	}

	public void invokeDownloadRuntimesWizard() {
		WorkbenchPreferenceDialog preferenceDialog = new WorkbenchPreferenceDialog();
		preferenceDialog.open();
		RuntimeDetectionPreferencePage runtimeDetectionPage = new RuntimeDetectionPreferencePage(preferenceDialog);
		preferenceDialog.select(runtimeDetectionPage);

		new PushButton(preferenceDialog, "Download...").click();
		new WaitUntil(new ShellIsAvailable("Download Runtimes"), TimePeriod.VERY_LONG);
		runtimeDownloadWizard = new WizardDialog("Download Runtimes");
	}

	public void processSelectingRuntime(String runtime) {
		TaskWizardFirstPage selectRuntimePage = new TaskWizardFirstPage(runtimeDownloadWizard);
		selectRuntimePage.selectRuntime(runtime);
		runtimeDownloadWizard.next();
	}

	public void processInsertingCredentials(String username, String password) {
		TaskWizardLoginPage credentialsPage = new TaskWizardLoginPage(runtimeDownloadWizard);
		assertEquals("Domain is set to jboss.org", "jboss.org", credentialsPage.getDomain());

		// username is not enabled -> we have to add credential
		if (!credentialsPage.containsUsername(username)) {
			credentialsPage.addCredentials(username, password);
		}
		new DefaultShell("Download Runtimes").setFocus();
		credentialsPage.setUsername(username);
		try{
			new WaitWhile(new JobIsRunning());
		}catch (WaitTimeoutExpiredException e){
			e.printStackTrace();
			throw e;
		}
		runtimeDownloadWizard.next();
		new WaitWhile(new JobIsRunning());
	}

	public void processRuntimeDownload() {
		
		TaskWizardThirdPage downloadRuntimePage = new TaskWizardThirdPage(runtimeDownloadWizard);
		downloadRuntimePage.setInstallFolder(tmpPath.getAbsolutePath());

		// wizard.finish(); -- does not work (Problem with slow downloading)
		new PushButton("Finish").click();
		runtimeDownloadWizard = null;

		new WaitUntil(new JobIsRunning(), TimePeriod.VERY_LONG, false);
		new WaitWhile(new JobIsRunning(), TimePeriod.getCustom(900));
		WorkbenchPreferenceDialog preferenceDialog = new WorkbenchPreferenceDialog();
		preferenceDialog.open();

		preferenceDialog.ok();
	}

	public void processLicenceAgreement() {
		TaskWizardSecondPage licenceAgreementPage = new TaskWizardSecondPage(runtimeDownloadWizard);
		licenceAgreementPage.acceptLicense(true);
		runtimeDownloadWizard.next();
	}

	private class SuccesfullyDeleted extends AbstractWaitCondition {

		private File dir;

		public SuccesfullyDeleted(File dir) {
			this.dir = dir;
		}

		@Override
		public boolean test() {
			try {
				log.debug("Trying to delete directory: "+dir.toString());
				FileUtils.deleteDirectory(dir);
			} catch (IOException e) {
				log.debug("Deletion was unsuccesfull");
				log.debug(e.getClass() + ": " +  e.getMessage() + ": " + e.getCause() + "\n" +  e.getStackTrace().toString());
				return false;
			}
			return true;
		}

		@Override
		public String description() {
			// TODO Auto-generated method stub
			return null;
		}

	}
	
	public void downloadRuntimeWithCredentials(String runtime) {
		downloadRuntimeWithCredentials(getUsername(), getPassword(), runtime, 1);
	}
	
	
	public String getUsername() {
		if(username == null || username.length() == 0) {
			fail("To download product runtime you have to set property \""+JBOSS_ORG_USERNAME_PROPERTY_KEY+"\"");
		}
		return username;
	}
	
	public String getPassword() {
		if(password == null || password.length() == 0) {
			fail("To download product runtime you have to set property \""+JBOSS_ORG_PASSWORD_PROPERTY_KEY+"\"");
		}
		return password;
	}
	
	public void downloadRuntimeWithCredentials(String username, String password, String runtime, int serversCount) {
		downloadRuntimeWithCredentials(runtime, username, password);
		checkRuntimes(serversCount);
	}
	
	public void downloadRuntimeWithCredentials(String runtime, String username, String password){
		invokeDownloadRuntimesWizard();
		processSelectingRuntime(runtime);
		
		if( username != null && password != null )
			processInsertingCredentials(username, password);
		
		processLicenceAgreement();
		processRuntimeDownload();
	}
	
	

	public void downloadRuntimeNoCredentials(String runtime){
		downloadRuntimeWithCredentials(runtime, null, null);
	}
	
	public void downloadAndCheckRuntime(String runtime, int serversCount) {
		downloadRuntimeNoCredentials(runtime);
		checkRuntimes(serversCount);
	}
	
	public void checkRuntimes(int serversCount) {
		assertServerRuntimesNumber(serversCount);
	}
	
	public void downloadAndCheckServer(String server, int serversCount) {
		downloadAndCheckRuntime(server, serversCount);
	}
	
	
}
