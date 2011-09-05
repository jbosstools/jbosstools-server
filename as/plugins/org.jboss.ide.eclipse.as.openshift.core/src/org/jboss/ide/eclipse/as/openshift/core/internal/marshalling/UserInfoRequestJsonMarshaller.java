package org.jboss.ide.eclipse.as.openshift.core.internal.marshalling;

import org.jboss.dmr.ModelNode;
import org.jboss.ide.eclipse.as.openshift.internal.core.request.UserInfoRequest;


public class UserInfoRequestJsonMarshaller implements IOpenshiftRequestMarshaller<UserInfoRequest> {

	private UserInfoRequest userInfoRequest;

	public UserInfoRequestJsonMarshaller(UserInfoRequest userInfoRequest) {
		this.userInfoRequest = userInfoRequest;
	}

	public String create() {
		ModelNode node = new ModelNode();
		node.get(IOpenshiftJsonConstants.PROPERTY_RHLOGIN).set(userInfoRequest.getRhLogin());
		node.get(IOpenshiftJsonConstants.PROPERTY_DEBUG).set(String.valueOf(userInfoRequest.isDebug()));
		return node.toJSONString(true);
	}
	
}
