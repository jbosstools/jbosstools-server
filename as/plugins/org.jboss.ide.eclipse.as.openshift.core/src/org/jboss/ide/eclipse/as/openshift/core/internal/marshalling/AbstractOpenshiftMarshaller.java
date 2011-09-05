package org.jboss.ide.eclipse.as.openshift.core.internal.marshalling;


public abstract class AbstractOpenshiftMarshaller<OPENSHIFTOBJECT extends IOpenshiftRequest> implements IOpenshiftMarshaller<OPENSHIFTOBJECT> {

	@Override
	public String marshall(OPENSHIFTOBJECT object) {
		StringBuilder builder = new StringBuilder();
		append(builder);
		return builder.toString();
	}

	protected abstract void append(StringBuilder builder);

}
