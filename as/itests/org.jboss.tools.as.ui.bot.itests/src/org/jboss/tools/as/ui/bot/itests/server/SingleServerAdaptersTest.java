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
package org.jboss.tools.as.ui.bot.itests.server;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.reddeer.junit.internal.runner.ParameterizedRequirementsRunnerFactory;
import org.eclipse.reddeer.junit.runner.RedDeerSuite;
import org.eclipse.reddeer.requirements.jre.JRERequirement.JRE;
import org.jboss.tools.as.ui.bot.itests.parametized.server.ServerAdaptersTest;
import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Parameterized.UseParametersRunnerFactory;

/**
 * JBIDE-26949 Tests creation of server adapter
 * 
 * @author jkopriva@redhat.com
 *
 */
@RunWith(RedDeerSuite.class)
@JRE(cleanup = true)
@UseParametersRunnerFactory(ParameterizedRequirementsRunnerFactory.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SingleServerAdaptersTest extends ServerAdaptersTest {

	@Parameters(name = "{0}")
	public static ArrayList<String> data() {
		ArrayList<String> list = new ArrayList<String>();
		list.add(System.getProperty("jbosstools.test.single.runtime.server.adapter.label"));
		return list;
	}

	public SingleServerAdaptersTest(String server) {
		super(server);
	}
	
	protected File getDownloadPath() {
		return new File(System.getProperty("jbosstools.test.single.runtime.location"));
	}

}
