/*******************************************************************************
 * Copyright (c) 2007-2019 Red Hat, Inc.
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
import org.eclipse.reddeer.eclipse.wst.server.ui.wizard.NewServerWizard;
import org.eclipse.reddeer.eclipse.wst.server.ui.wizard.NewServerWizardPage;
import org.eclipse.reddeer.junit.internal.runner.ParameterizedRequirementsRunnerFactory;
import org.eclipse.reddeer.junit.runner.RedDeerSuite;
import org.eclipse.reddeer.requirements.jre.JRERequirement.JRE;
import org.eclipse.reddeer.workbench.handler.WorkbenchShellHandler;
import org.jboss.ide.eclipse.as.reddeer.server.wizard.page.JBossRuntimeWizardPage;
import org.jboss.ide.eclipse.as.reddeer.server.wizard.page.NewServerAdapterPage;
import org.jboss.tools.as.ui.bot.itests.AbstractTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Parameterized.UseParametersRunnerFactory;

/**
 * JBIDE-26853
 * Tests creation of recent server adapters
 * 
 * @author jkopriva@redhat.com
 *
 */
@RunWith(RedDeerSuite.class)
@JRE(cleanup=true)
@UseParametersRunnerFactory(ParameterizedRequirementsRunnerFactory.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ServerAdaptersTest extends AbstractTest {

	private final Logger LOGGER = Logger.getLogger(this.getClass());
	
	public static final String WILDFLY_FAMILY = "JBoss Community";
	public static final String EAP_FAMILY = "Red Hat JBoss Middleware";

	@Parameters(name = "{0}")
	public static ArrayList<String> data() {
		ArrayList<String> list = new ArrayList<String>();
		list.add("WildFly 19");
		list.add("WildFly 20");
		list.add("WildFly 21");
		list.add("Red Hat JBoss Enterprise Application Platform 7.0");
		list.add("Red Hat JBoss Enterprise Application Platform 7.1");
		list.add("Red Hat JBoss Enterprise Application Platform 7.2");
		list.add("Red Hat JBoss Enterprise Application Platform 7.3");

		return list;
	}

	@BeforeClass
	public static void prepareWorkspace() {
		deleteRuntimes();
	}

	private String server;

	public ServerAdaptersTest(String server) {
		this.server = server;
	}

	@Test
	public void setupLocalServerAdapter() {
		NewServerWizard serverW = new NewServerWizard();
		try {
			serverW.open();

			NewServerWizardPage sp = new NewServerWizardPage(serverW);

			sp.selectType(getFamily(server), server);

			serverW.next();

			NewServerAdapterPage ap = new NewServerAdapterPage(serverW);
			ap.setRuntime(null);
			ap.checkErrors();

			serverW.next();

			setupRuntime(serverW);

			serverW.finish();
		} catch (AssertionError | RuntimeException e) {
			try {
				serverW.cancel();
			} catch (RedDeerException ex) {
				LOGGER.error("Cannot close server wizard!", ex);
			}
			throw e;
		} 
	}

	private String getFamily(String server) {
		if (server.contains("WildFly")) {
			return WILDFLY_FAMILY;
		} else {
			return EAP_FAMILY;
		}
	}

	protected void setupRuntime(NewServerWizard wizard) {
		JBossRuntimeWizardPage rp = new JBossRuntimeWizardPage(wizard);
		rp.setRuntimeName(this.server + " Runtime");
		rp.setRuntimeDir(getDownloadPath().getAbsolutePath());
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
