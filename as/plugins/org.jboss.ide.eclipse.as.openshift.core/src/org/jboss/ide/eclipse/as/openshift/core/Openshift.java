package org.jboss.ide.eclipse.as.openshift.core;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.osgi.util.NLS;
import org.jboss.dmr.ModelNode;
import org.jboss.ide.eclipse.as.openshift.core.internal.marshalling.OpenshiftJsonRequestFactory;
import org.jboss.ide.eclipse.as.openshift.core.internal.marshalling.UserInfoRequestJsonMarshaller;
import org.jboss.ide.eclipse.as.openshift.internal.core.HttpClientException;
import org.jboss.ide.eclipse.as.openshift.internal.core.UrlConnectionHttpClient;
import org.jboss.ide.eclipse.as.openshift.internal.core.request.UserInfoRequest;
import org.jboss.ide.eclipse.as.openshift.internal.core.utils.UrlBuilder;

public class Openshift {

	private static final String BASE_URL = "https://openshift.redhat.com/broker";

	private String username;
	private String password;

	public Openshift(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public UserInfo getUserInfo() throws OpenshiftException {
		UrlBuilder userInfoUrlBuilder = new UrlBuilder(BASE_URL).path("userinfo");
		try {
			String userInfoRequest = new UserInfoRequestJsonMarshaller().marshall(new UserInfoRequest(username, true));
			IHttpClient iHttpClient = createHttpClient(userInfoUrlBuilder.toUrl());
			String request = new OpenshiftJsonRequestFactory(password, userInfoRequest).create();
			String userInfoResponse = iHttpClient.post(request);
			ModelNode userInfoReponse = ModelNode.fromJSONString(userInfoResponse);
			return new UserInfo(
					userInfoReponse.get("rhlogin").asString(),
					userInfoReponse.get("uuid").asString(),
					userInfoReponse.get("ssh_key").asString(),
					userInfoReponse.get("rhc_domain").asString(),
					userInfoReponse.get("namespace").asString());
		} catch (MalformedURLException e) {
			throw new OpenshiftException(
					NLS.bind("Could not get user info for user \"{0}\" at \"{1}\"", username,
							userInfoUrlBuilder.toString()), e);
		} catch (HttpClientException e) {
			throw new OpenshiftException(
					NLS.bind("Could not get user info for user \"{0}\" at \"{1}\"", username,
							userInfoUrlBuilder.toString()), e);
		}


	}

	private IHttpClient createHttpClient(URL url) {
		return new UrlConnectionHttpClient(url);
	}

}
