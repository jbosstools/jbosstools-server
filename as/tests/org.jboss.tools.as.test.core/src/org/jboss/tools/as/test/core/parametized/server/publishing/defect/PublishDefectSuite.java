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
package org.jboss.tools.as.test.core.parametized.server.publishing.defect;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	PublishWeb2DeletesWeb1LibsTest.class, 
	ClosedProjectPublishTest.class,
	PublishRemovalMarkerDefectTest.class,
	UnchangedUtilRestartedDefectTest.class,
	UnchangedJarRestartedDefectTest.class,
	UtilInWebPathDefectTest.class,
	NonAsciiInUtilJarPublishDefectTest.class
})
public class PublishDefectSuite {
}
