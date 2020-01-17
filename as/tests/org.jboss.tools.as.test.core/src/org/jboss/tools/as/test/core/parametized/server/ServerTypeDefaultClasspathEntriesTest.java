/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.test.core.parametized.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.classpath.core.runtime.CustomRuntimeClasspathModel;
import org.jboss.ide.eclipse.as.classpath.core.runtime.IRuntimePathProvider;
import org.jboss.tools.as.test.core.internal.utils.ServerParameterUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class ServerTypeDefaultClasspathEntriesTest extends Assert {
	

	 private String serverType;
	 public ServerTypeDefaultClasspathEntriesTest(String serverType) {
		 this.serverType = serverType;
	 }
	 @Parameters(name = "{0}")
	 public static Collection<Object[]> data() {
		 return ServerParameterUtils.asCollection(ServerParameterUtils.getJBossServerTypeParameters());
	 }
	@Test
	public void testDefaultClasspathEntriesAdded() {
		assertNotNull(serverType);
		IServerType type = ServerCore.findServerType(serverType);
		if( type == null )
			fail("Server type " + type + " not found in the build");
		if( type.getRuntimeType() == null ) 
			fail("Server type " + serverType + " does not have an associated runtime");
		IRuntimeType rtType = type.getRuntimeType();
		IRuntimePathProvider[] providers = CustomRuntimeClasspathModel.getInstance().getDefaultEntries(rtType);
		assertNotNull("Null returned for default classpath entries", providers);
		assertFalse("0 classpath entr yproviders returned", providers.length == 0);
		ArrayList<IPath> paths = new ArrayList<IPath>();
		for( int i = 0; i < providers.length; i++ ) {
			paths.addAll(Arrays.asList(providers[i].getAbsolutePaths()));
		}
		assertFalse("0 classpath entries returned", paths.size() == 0);
	}

}
