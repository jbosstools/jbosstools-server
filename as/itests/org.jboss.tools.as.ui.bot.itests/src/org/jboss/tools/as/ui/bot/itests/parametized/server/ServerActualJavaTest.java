 /*******************************************************************************
 * Copyright (c) 2007-2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v 1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.ui.bot.itests.parametized.server;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.reddeer.common.exception.RedDeerException;
import org.eclipse.reddeer.common.logging.Logger;
import org.eclipse.reddeer.common.matcher.VersionMatcher;
import org.eclipse.reddeer.common.wait.AbstractWait;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.direct.preferences.Preferences;
import org.eclipse.reddeer.eclipse.wst.server.ui.wizard.NewServerWizard;
import org.eclipse.reddeer.eclipse.wst.server.ui.wizard.NewServerWizardPage;
import org.eclipse.reddeer.junit.annotation.RequirementRestriction;
import org.eclipse.reddeer.junit.internal.runner.ParameterizedRequirementsRunnerFactory;
import org.eclipse.reddeer.junit.requirement.matcher.RequirementMatcher;
import org.eclipse.reddeer.junit.runner.RedDeerSuite;
import org.eclipse.reddeer.requirements.jre.JRERequirement.JRE;
import org.eclipse.reddeer.swt.impl.button.RadioButton;
import org.eclipse.reddeer.swt.impl.combo.DefaultCombo;
import org.eclipse.reddeer.swt.impl.group.DefaultGroup;
import org.eclipse.reddeer.workbench.handler.WorkbenchShellHandler;
import org.jboss.ide.eclipse.as.reddeer.server.wizard.page.JBossRuntimeWizardPage;
import org.jboss.ide.eclipse.as.reddeer.server.wizard.page.NewServerAdapterPage;
import org.jboss.tools.as.ui.bot.itests.AbstractTest;
import org.jboss.tools.as.ui.bot.itests.reddeer.util.OperateServerTemplate;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Parameterized.UseParametersRunnerFactory;


/**
 * JBIDE-28305
 * 
 * @author olkornii@redhat.com
 *
 */
@RunWith(RedDeerSuite.class)
@JRE(cleanup=true, setDefault=true)
@UseParametersRunnerFactory(ParameterizedRequirementsRunnerFactory.class)
public class ServerActualJavaTest extends AbstractTest {
	
private final Logger LOGGER = Logger.getLogger(this.getClass());
	
	public static final String WILDFLY_FAMILY = "JBoss Community";
	public static final String EAP_FAMILY = "Red Hat JBoss Middleware";
	
	@Parameters(name = "{0}")
	public static ArrayList<String> data() {
		ArrayList<String> list = new ArrayList<String>();

		list.add("WildFly 27");
		list.add("Red Hat JBoss Enterprise Application Platform 8.0");

		return list;
	}
	
	@RequirementRestriction
	public static RequirementMatcher getRestrictionMatcher() {
	  return new RequirementMatcher(JRE.class, "version", new VersionMatcher(">1.8"));
	}

	@BeforeClass
	public static void prepareWorkspace() {
		Preferences.set("org.eclipse.debug.ui", "Console.limitConsoleOutput", "false");
		deleteRuntimes();
	}

	private String server;

	public ServerActualJavaTest(String server) {
		this.server = server;
	}
    
	@Test
	public void setupAndRunLocalServer() {
		NewServerWizard serverW = new NewServerWizard();
		try {
			serverW.open();

			NewServerWizardPage sp = new NewServerWizardPage(serverW);

			sp.selectType(getFamily(server), getServerName(server));
			sp.setName(server);

			serverW.next();

			NewServerAdapterPage ap = new NewServerAdapterPage(serverW);
			ap.setRuntime(null);
			ap.checkErrors();

			serverW.next();

			setupRuntime(serverW);

			AbstractWait.sleep(TimePeriod.DEFAULT);
			
			serverW.finish();
		} catch (AssertionError | RuntimeException e) {
			try {
				serverW.cancel();
			} catch (RedDeerException ex) {
				LOGGER.error("Cannot close server wizard!", ex);
			}
			throw e;
		}
		
		OperateServerTemplate operate = new OperateServerTemplate(server);
    	operate.setUp();
    	try {
    		operate.operateServer();
    	} finally {
    		operate.cleanServerAndConsoleView();
    	}
	}

	private String getFamily(String server) {
		if (server.contains("WildFly")) {
			return WILDFLY_FAMILY;
		} else {
			return EAP_FAMILY;
		}
	}

	private String getServerName(String server) {
		return AbstractTest.getServerNameForServerString(server);
	}

	protected void setupRuntime(NewServerWizard wizard) {
		JBossRuntimeWizardPage rp = new JBossRuntimeWizardPage(wizard);
		rp.setRuntimeName(this.server + " Runtime");
		rp.setRuntimeDir(getDownloadPath().getAbsolutePath());
		new RadioButton("Alternate JRE: ").toggle(true);
		DefaultGroup r_JRE = new DefaultGroup("Runtime JRE");
		DefaultCombo r_COMBO = new DefaultCombo(r_JRE, 1);
		r_COMBO.setSelection(1);
	}

	protected File getDownloadPath() {
		return new File(getServerHome(this.server));
	}

	private String getServerHome(String server) {
		String homeFlag;
		if (server.contains("WildFly")) {
			String version = server.split(" ")[1];
			homeFlag = Arrays.stream(PomServerConstants.getJBossHomeFlags()).filter(x -> x.contains(version))
					.findFirst().orElse(null);
		} else {
			String version = server.split(" ")[6];
			homeFlag = Arrays.stream(PomServerConstants.getJBossHomeFlags()).filter(x -> (x.contains(version) && x.contains("eap")))
					.findFirst().orElse(null);
		}
		return System.getProperty(homeFlag);
	}

	@After
	public void deleteWorkspace() {
		deleteRuntimes();
	}
	
	@AfterClass
	public static void closeAll() {
		WorkbenchShellHandler.getInstance().closeAllNonWorbenchShells();
	}
}
