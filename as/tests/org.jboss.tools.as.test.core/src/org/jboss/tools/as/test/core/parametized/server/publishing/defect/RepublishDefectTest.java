/******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.as.test.core.parametized.server.publishing.defect;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.tools.as.test.core.internal.utils.ResourceUtils;
import org.jboss.tools.as.test.core.internal.utils.wtp.CreateProjectOperationsUtility;
import org.jboss.tools.as.test.core.internal.utils.wtp.JavaEEFacetConstants;
import org.jboss.tools.as.test.core.internal.utils.wtp.OperationTestCase;
import org.jboss.tools.as.test.core.parametized.server.publishing.AbstractPublishingTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class RepublishDefectTest extends AbstractPublishingTest {
	public static int count = 1;
	
	@Parameters
	public static Collection<Object[]> params() {
		return defaultData();
	}
	
	private int myCount;
	public RepublishDefectTest(String serverType, String zip,
			String deployLoc, String perMod) {
		super(serverType, zip, deployLoc, perMod);
		myCount = count;
		count++;
	}

	protected void createProjects() throws Exception {
    	IDataModel dm = CreateProjectOperationsUtility.getEARDataModel(ap("ear"), "thatContent", null, null, JavaEEFacetConstants.EAR_5, true);
    	OperationTestCase.runAndVerify(dm);
    	IDataModel dyn1Model = CreateProjectOperationsUtility.getWebDataModel(ap("d1v"), ap("ear"), null, null, null, JavaEEFacetConstants.WEB_23, true);
    	OperationTestCase.runAndVerify(dyn1Model);
    	IDataModel dyn2Model = CreateProjectOperationsUtility.getWebDataModel(ap("d2v"), ap("ear"), null, null, null, JavaEEFacetConstants.WEB_23, true);
    	OperationTestCase.runAndVerify(dyn2Model);
    	addModuleToServer(ServerUtil.getModule(findProject(ap("ear"))));
	}
	
	/* Append myCount as a suffix to this original*/
	private String ap(String original) {
		return original + myCount;
	}
	
	protected void completeSetUp() {
		// Keep it local for REAL publishes
		wc.setAttribute(IDeployableServer.SERVER_MODE, "local");
	}

	private void addOrRemoveModuleWithPublish(IModule[] module, boolean add) throws CoreException {
		IServerWorkingCopy wc = server.createWorkingCopy();
		if( add )
			wc.modifyModules(module, new IModule[]{}, new NullProgressMonitor());
		else
			wc.modifyModules(new IModule[]{}, module, new NullProgressMonitor());
		server = wc.save(true, new NullProgressMonitor());
		server.publish(IServer.PUBLISH_INCREMENTAL, new NullProgressMonitor());
	}
	
	
	@Test
	public void testJBIDE6184_Odd_Republish_Error() throws Exception {
		IModule[] ear = getModule(findProject(ap("ear")));
		
    	// Create a temp server
		addOrRemoveModuleWithPublish(ear, true);
		
		// verify 
		IPath earPath = getLocalPublishMethodDeployRoot();
		System.out.println(earPath);
		JBIDE6184EarHasDynProjs(earPath, true);
		
		// undeploy
		addOrRemoveModuleWithPublish(ear, false);
		assertFalse(earPath.toFile().exists());
		
		ResourceUtils.deleteProject(ap("d1v"));
		
		// republish the ear
		addOrRemoveModuleWithPublish(ear, true);
		JBIDE6184EarHasDynProjs(earPath, false);
		
		// recreate the war
		IDataModel dyn1Model = CreateProjectOperationsUtility.getWebDataModel(ap("d1v"), ap("ear"), null, null, null, JavaEEFacetConstants.WEB_23, true);
    	OperationTestCase.runAndVerify(dyn1Model);
		server.publish(IServer.PUBLISH_INCREMENTAL, new NullProgressMonitor());
		JBIDE6184EarHasDynProjs(earPath, true);
	}
	
	protected void JBIDE6184EarHasDynProjs(IPath earPath, boolean d1vPresent ) {
		ArrayList<IPath> mustBePresent = new ArrayList<IPath>();
		ArrayList<IPath> mustBeMissing = new ArrayList<IPath>();
		
		mustBePresent.add(earPath);
		mustBePresent.add(earPath.append("META-INF"));
		mustBePresent.add(earPath.append("META-INF").append("application.xml"));
		
		ArrayList<IPath> tmp = d1vPresent ? mustBePresent : mustBeMissing;
		String d1WarName = ap("d1v") + ".war";
		tmp.add(earPath.append(d1WarName));
		tmp.add(earPath.append(d1WarName).append("WEB-INF"));
		tmp.add(earPath.append(d1WarName).append("META-INF"));
		tmp.add(earPath.append(d1WarName).append("META-INF").append("MANIFEST.MF"));
		tmp.add(earPath.append(d1WarName).append("WEB-INF").append("web.xml"));

		String d2WarName = ap("d2v") + ".war";
		mustBePresent.add(earPath.append(d2WarName));
		mustBePresent.add(earPath.append(d2WarName).append("WEB-INF"));
		mustBePresent.add(earPath.append(d2WarName).append("META-INF"));
		mustBePresent.add(earPath.append(d2WarName).append("META-INF").append("MANIFEST.MF"));
		mustBePresent.add(earPath.append(d2WarName).append("WEB-INF").append("web.xml"));
		
		verifyList(earPath, mustBePresent, true);
		verifyList(earPath, mustBeMissing, false);
	}
}
