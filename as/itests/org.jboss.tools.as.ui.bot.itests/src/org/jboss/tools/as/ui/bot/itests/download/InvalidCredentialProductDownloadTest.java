package org.jboss.tools.as.ui.bot.itests.download;

import static org.junit.Assert.fail;

import org.eclipse.reddeer.common.condition.AbstractWaitCondition;
import org.eclipse.reddeer.common.exception.WaitTimeoutExpiredException;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.core.exception.CoreLayerException;
import org.eclipse.reddeer.swt.api.Shell;
import org.eclipse.reddeer.swt.condition.ShellIsAvailable;
import org.eclipse.reddeer.swt.impl.button.CancelButton;
import org.eclipse.reddeer.swt.impl.progressbar.DefaultProgressBar;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.eclipse.reddeer.swt.impl.text.DefaultText;
import org.eclipse.reddeer.workbench.ui.dialogs.WorkbenchPreferenceDialog;
import org.jboss.tools.as.ui.bot.itests.reddeer.util.DisableSecureStorageRequirement.DisableSecureStorage;
import org.junit.Test;

/**
 * Download of runtime via $0 subscription download
 * 
 * Downloads several product runtimes and checks if they were successfully downloaded and added
 * 
 * @author Radoslav Rabara
 *
 */

@DisableSecureStorage
public class InvalidCredentialProductDownloadTest {

	/**
	 * Test downloading of runtime via $0 subscription download with invalid
	 * credentials
	 */
	@Test
	public void useInvalidCredentials() {
		RuntimeDownloadTestUtility util = new RuntimeDownloadTestUtility();
		util.invokeDownloadRuntimesWizard();

		util.processSelectingRuntime("JBoss EAP 6.2.0");
		util.processInsertingCredentials("Invalid username", "Invalid password");
		new WaitWhile(new ValidatingCredentialsProgressBarIsRunning());
		
		assertErrorMessageIsShown();
		
		Shell downloadRuntimesShell = new DefaultShell("Download Runtimes");
		new CancelButton(downloadRuntimesShell).click();
		new WaitWhile(new ShellIsAvailable(downloadRuntimesShell));
		new WorkbenchPreferenceDialog().ok();
	}
	

	private void assertErrorMessageIsShown() {
		try{
			new WaitUntil(new ErrorMessageIsShown());
		}catch (WaitTimeoutExpiredException e){
			e.printStackTrace();
			fail("Error message was not shown. "+e.getMessage());
		}
	}

	private class ErrorMessageIsShown extends AbstractWaitCondition{
		
		@Override
		public boolean test() {
			try {
				new DefaultText(" Your credentials are incorrect. Please review the values and try again.");
				return true;
			} catch (CoreLayerException e) {
				return false;
			}
		}
		
		@Override
		public String errorMessageWhile() {
			return "error message was not shown.";
		}
		
		@Override
		public String description() {
			return "error message is shown.";
		}
	}
	
	private class ValidatingCredentialsProgressBarIsRunning extends AbstractWaitCondition{

		@Override
		public boolean test() {
			try{
				new DefaultProgressBar("Validating Credentials");
				return true;
			}catch(CoreLayerException e){
				return false;
			}
		}

		@Override
		public String description() {
			return "Validating Credentials progress bar is running";
		}

		@Override
		public String errorMessageUntil() {
			return "Validating Credentials progress bar is not running";
		}
		
	}
}
