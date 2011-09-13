package org.jboss.ide.eclipse.as.openshift.internal.core.marshalling;


public abstract class AbstractOpenshiftMarshaller<REQUEST extends IOpenshiftRequest> implements IOpenshiftMarshaller<REQUEST> {

	@Override
	public String marshall(REQUEST object) {
		StringBuilder builder = new StringBuilder();
		append(builder);
		return builder.toString();
	}

	protected abstract void append(StringBuilder builder);

}
