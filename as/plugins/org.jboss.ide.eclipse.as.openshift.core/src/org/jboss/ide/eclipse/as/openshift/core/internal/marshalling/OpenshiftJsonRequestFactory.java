package org.jboss.ide.eclipse.as.openshift.core.internal.marshalling;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;

public class OpenshiftJsonRequestFactory {

	private static final char EQ = '=';
	private static final String PROPERTY_PASSWORD = "password";
	private static final String PROPERTY_JSON_DATA = "json_data";
	private static final String DATA_ENCODING = "UTF-8";
	private static final char AMP = '&';
	private String[] payloads;
	private String password;

	public OpenshiftJsonRequestFactory(String password, String... payloads) {
		this.password = password;
		this.payloads = payloads;
	}

	public String create() throws OpenshiftException {
		try {
			StringBuilder builder = new StringBuilder();
			appendPassword(builder);
			builder.append(AMP);
			appendPayload(builder);
			return builder.toString();
		} catch (UnsupportedEncodingException e) {
			throw new OpenshiftException("Could not create request", e);
		}
	}

	private void appendPassword(StringBuilder builder) throws UnsupportedEncodingException {
		builder.append(PROPERTY_PASSWORD)
				.append(EQ)
				.append(URLEncoder.encode(password, DATA_ENCODING));
	}

	private void appendPayload(StringBuilder builder) throws UnsupportedEncodingException {
		StringBuilder payloadBuilder = new StringBuilder();
		for (int i = 0; i < payloads.length; i++) {
			if (i > 0
					&& i < payloads.length + 1) {
				payloadBuilder.append(AMP);
			}
			payloadBuilder.append(payloads[i]);
		}

		if (builder.length() > 0) {
			builder
					.append(PROPERTY_JSON_DATA)
					.append(EQ)
					.append(URLEncoder.encode(payloadBuilder.toString(), DATA_ENCODING));
		}

	}
}
