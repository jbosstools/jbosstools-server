package org.jboss.ide.eclipse.as.openshift.core.internal.marshalling;

import org.jboss.ide.eclipse.as.openshift.internal.core.request.UserInfoRequest;

public interface IOpenshiftRequestMarshaller<REQUEST> {

	public String marshall(REQUEST request);
	
}
