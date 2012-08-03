package org.jboss.tools.as.test.core.parametized.server.publishing.defect;

import org.jboss.tools.as.test.core.parametized.server.publishing.defect.RepublishDefectTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	PublishWeb2DeletesWeb1LibsTest.class, 
	RepublishDefectTest.class
})
public class PublishDefectSuite {
}
