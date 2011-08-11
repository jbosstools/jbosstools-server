package org.jboss.ide.eclipse.as.test.publishing.v2;
/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
import java.io.IOException;
import java.util.Arrays;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.datamodel.properties.IAddReferenceDataModelProperties;
import org.eclipse.wst.common.componentcore.internal.operation.AddReferenceDataModelProvider;
import org.eclipse.wst.common.componentcore.internal.resources.VirtualReference;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.frameworks.datamodel.IDataModelProvider;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;
import org.jboss.ide.eclipse.as.test.util.wtp.JavaEEFacetConstants;
import org.jboss.ide.eclipse.as.test.util.wtp.OperationTestCase;
import org.jboss.ide.eclipse.as.test.util.wtp.ProjectCreationUtil;

public class MockJSTPublisherTestDynUtil extends MockJSTPublisherTest {
	protected String getModuleName() {
		return "newModule5";
	}
	protected IProject createProject() throws Exception {
		IDataModel dm = ProjectCreationUtil.getWebDataModel(getModuleName(), null, null, 
				getContentDir(), null, JavaEEFacetConstants.WEB_25, false);
		OperationTestCase.runAndVerify(dm);
		IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(getModuleName());
		System.out.println(p.getLocation().toOSString());

		IDataModel dm2 = ProjectCreationUtil.getUtilityProjectCreationDataModel("util", null);
		OperationTestCase.runAndVerify(dm2);
		IProject p2 = ResourcesPlugin.getWorkspace().getRoot().getProject("util");
		System.out.println(p2.getLocation().toOSString());
		
		final IVirtualComponent webComponent = ComponentCore.createComponent(p);
		
		final IVirtualReference ref = new VirtualReference(webComponent, 
				ComponentCore.createComponent(p2, false));
		ref.setArchiveName("util.jar");
		ref.setRuntimePath(new Path("WEB-INF").append("lib").makeAbsolute());
		
		IWorkspaceRunnable runnable = new IWorkspaceRunnable(){
			public void run(IProgressMonitor monitor) throws CoreException{
				addOneReference(webComponent, ref);
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());
		} catch( CoreException e ) {
			throw e;
		}
		
		return p;
	}
	
	protected void addOneReference(IVirtualComponent parent, IVirtualReference ref) throws CoreException {
		IDataModelProvider provider = new AddReferenceDataModelProvider();
		IDataModel dm = DataModelFactory.createDataModel(provider);
		dm.setProperty(IAddReferenceDataModelProperties.SOURCE_COMPONENT, parent);
		dm.setProperty(IAddReferenceDataModelProperties.TARGET_REFERENCE_LIST, Arrays.asList(ref));
		
		IStatus stat = dm.validateProperty(IAddReferenceDataModelProperties.TARGET_REFERENCE_LIST);
		if (!stat.isOK())
			throw new CoreException(stat);
		try {
			IStatus s = dm.getDefaultOperation().execute(new NullProgressMonitor(), null);
			if (!stat.isOK())
				throw new CoreException(stat);
		} catch(Exception e) {
			fail();
		}
	}

	
	protected void theTest(boolean isAs7) throws CoreException, IOException {

		IModule mod = ServerUtil.getModule(project);
		server = ServerRuntimeUtils.addModule(server, mod);
		ServerRuntimeUtils.publish(server);
		String[] nonAS7 = new String[]{
				"newModule5.war", "newModule5.war/META-INF", "newModule5.war/META-INF/MANIFEST.MF", 
				"newModule5.war/WEB-INF/classes", "newModule5.war/WEB-INF/lib", "newModule5.war/WEB-INF/web.xml", 
				"newModule5.war/WEB-INF/lib/util.jar"
		};

		String[] as7 = new String[]{
				"newModule5.war", "newModule5.war/META-INF", "newModule5.war/META-INF/MANIFEST.MF", 
				"newModule5.war/WEB-INF/classes", "newModule5.war/WEB-INF/lib", "newModule5.war/WEB-INF/web.xml", 
				"newModule5.war/WEB-INF/lib/util.jar", "newModule5.war.dodeploy"
		};

		assertChanged( isAs7, nonAS7, as7 ); 
		MockPublishMethod.reset();
	}
}
