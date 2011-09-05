package org.jboss.ide.eclipse.as.openshift.core.internal.marshalling;


public interface IOpenshiftMarshaller<OBJECT extends IOpenshiftRequest> {

	public String marshall(OBJECT object);
	
}
