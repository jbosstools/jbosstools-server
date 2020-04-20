/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
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
import org.jboss.tools.as.ui.bot.itests.AbstractTest;
import org.jboss.tools.as.ui.bot.itests.parametized.server.ServerRuntimeUIConstants;
import org.jboss.tools.as.ui.bot.itests.reddeer.util.DisableSecureStorageRequirement.DisableSecureStorage;
import org.jboss.tools.common.util.PlatformUtil;
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
public class InvalidCredentialProductDownloadTest extends AbstractTest {

	/**
	 * Test downloading of runtime via $0 subscription download with invalid
	 * credentials
	 */
	@Test
	public void useInvalidCredentials() {
		RuntimeDownloadTestUtility util = new RuntimeDownloadTestUtility();
		util.invokeDownloadRuntimesWizard();

		util.processSelectingRuntime(ServerRuntimeUIConstants.JBEAP_700);
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
				if (PlatformUtil.isWindows()) {
					new DefaultText(" Your credentials have failed to validate.\r\nServer Message: Please, use a valid user name and password!\r\n");
				} else {
					new DefaultText(" Your credentials have failed to validate.\nServer Message: Please, use a valid user name and password!\n");
				}
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
